package com.uniajc.modelo;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.dao.ProcedimientosDAO;
import com.uniajc.dao.ProcedimientosDAO.ResultadoSP;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ProcedimientosDAOTest {
    private static ProcedimientosDAO dao;
    private static int idCliente;
    private static int idVendedor;
    private static int idAutorizador;
    private static int idProducto;
    private static BigDecimal precioProducto;
    private static Integer idVentaGenerada;
    private static Integer idMovimientoGenerado;

    @BeforeAll
    static void inicializarDatosPrueba() throws Exception {
        dao = new ProcedimientosDAO();
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();

        try (PreparedStatement ps = conn.prepareStatement("SELECT id_cliente FROM public.cliente LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Debe existir al menos un cliente en la BD");
            idCliente = rs.getInt(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id_usuario FROM public.usuario WHERE id_rol = 2 AND id_est_usuario = 1 LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Debe existir un Vendedor activo (id_rol=2, id_est_usuario=1)");
            idVendedor = rs.getInt(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id_usuario FROM public.usuario WHERE id_rol = 4 AND id_est_usuario = 1 LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Debe existir un Autorizador de Kardex activo (id_rol=4, id_est_usuario=1)");
            idAutorizador = rs.getInt(1);
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id_producto, precio_unitario FROM public.producto " +
                "WHERE id_est_producto = 1 AND (stock_actual - stock_reservado) >= 2 LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Debe existir un producto activo con al menos 2 unidades libres");
            idProducto = rs.getInt(1);
            precioProducto = rs.getBigDecimal(2);
        }
    }

    @Test
    @Order(1)
    void testRegistrarVentaFalloStockInsuficiente() throws Exception {
        String json = "[{\"id_producto\": " + idProducto + ", \"cantidad\": 999999}]";
        ResultadoSP res = dao.registrarVenta(idCliente, idVendedor, "Contado",
                BigDecimal.ZERO, BigDecimal.ZERO, json);

        System.out.println("[JUnit 1 - Venta sin stock] " + res);
        assertFalse(res.isExitoso());
        assertNull(res.getIdGenerado());
    }

    @Test
    @Order(2)
    void testRegistrarVentaExitosa() throws Exception {
        String json = "[{\"id_producto\": " + idProducto + ", \"cantidad\": 1}]";
        ResultadoSP res = dao.registrarVenta(idCliente, idVendedor, "Credito",
                BigDecimal.ZERO, BigDecimal.ZERO, json);

        System.out.println("[JUnit 2 - Venta exitosa] " + res);
        assertTrue(res.isExitoso());
        assertNotNull(res.getIdGenerado());
        idVentaGenerada = res.getIdGenerado();

        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT m.id_movimiento FROM public.movimiento_inventario m " +
                "JOIN public.detalle_venta d ON d.id_detalle_venta = m.id_detalle_venta " +
                "WHERE d.id_venta = ? LIMIT 1")) {
            ps.setInt(1, idVentaGenerada);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "La venta debió crear su movimiento de Kardex pendiente");
                idMovimientoGenerado = rs.getInt(1);
            }
        }
    }

    @Test
    @Order(3)
    void testAutorizarKardexExitoso() throws Exception {
        ResultadoSP res = dao.decidirMovimientoKardex(
                idMovimientoGenerado, idAutorizador, "AUTORIZAR", "Aprobado desde prueba JUnit");

        System.out.println("[JUnit 3 - Autorizar Kardex] " + res);
        assertTrue(res.isExitoso());
    }

    @Test
    @Order(4)
    void testAutorizarKardexDuplicadoDebeFallar() throws Exception {
        ResultadoSP res = dao.decidirMovimientoKardex(
                idMovimientoGenerado, idAutorizador, "AUTORIZAR", "Intento duplicado");

        System.out.println("[JUnit 4 - Idempotencia Kardex] " + res);
        assertFalse(res.isExitoso());
    }

    @Test
    @Order(5)
    void testRegistrarPagoExcedidoDebeFallar() throws Exception {
        BigDecimal montoExcedido = precioProducto.add(new BigDecimal("1000.00"));
        ResultadoSP res = dao.registrarPago(null, idVentaGenerada, montoExcedido,
                1, 2, idVendedor, "REF-EXC", "PAGO TOTAL");

        System.out.println("[JUnit 5 - Pago excedido] " + res);
        assertFalse(res.isExitoso());
    }

    @Test
    @Order(6)
    void testRegistrarPagoTotalExitoso() throws Exception {
        ResultadoSP res = dao.registrarPago(null, idVentaGenerada, precioProducto,
                1, 2, idVendedor, "REF-JUNIT-01", "PAGO TOTAL");

        System.out.println("[JUnit 6 - Pago total] " + res);
        assertTrue(res.isExitoso());
        assertEquals(0, BigDecimal.ZERO.compareTo(res.getNuevoSaldo()));
    }
    
}
