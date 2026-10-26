// Autor: Alejandro Mosquera | Commit 3: DAO Transaccional de Venta, DetalleVenta y salida en Kardex
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    /**
     * Registra en una única transacción (connection.setAutoCommit(false)):
     * 1. Cabecera de Venta
     * 2. Líneas de DetalleVenta
     * 3. Movimiento de salida por Venta Confirmada en el Kardex y descuento atómico de stock_actual (RN-01, RN-06)
     */
    public boolean registrarVentaTransaccional(Venta venta) throws SQLException {
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 1. Insertar cabecera de venta
            String sqlVenta = "INSERT INTO public.venta " +
                              "(fecha_venta, tipo_venta, subtotal, descuento, impuesto, total_venta, saldo_pendiente, " +
                              "id_cliente, id_usuario, id_est_venta) " +
                              "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_venta";

            try (PreparedStatement psVenta = conn.prepareStatement(sqlVenta)) {
                psVenta.setString(1, venta.getTipoVenta());
                psVenta.setBigDecimal(2, venta.getSubtotal());
                psVenta.setBigDecimal(3, venta.getDescuento());
                psVenta.setBigDecimal(4, venta.getImpuesto());
                psVenta.setBigDecimal(5, venta.getTotalVenta());
                psVenta.setBigDecimal(6, venta.getSaldoPendiente());
                psVenta.setInt(7, venta.getCliente().getIdCliente());
                psVenta.setInt(8, venta.getVendedor().getIdUsuario());
                psVenta.setInt(9, venta.getEstado().getId());

                try (ResultSet rsV = psVenta.executeQuery()) {
                    if (rsV.next()) {
                        venta.setIdVenta(rsV.getInt(1));
                    } else {
                        throw new SQLException("No se pudo generar el ID de la venta.");
                    }
                }
            }

            // 2. Por cada detalle: validar stock con bloqueo FOR UPDATE, insertar detalle, asentar Kardex y descontar stock
            String sqlLockProd = "SELECT stock_actual, stock_reservado, nombre_producto FROM public.producto WHERE id_producto = ? FOR UPDATE";
            String sqlDetalle = "INSERT INTO public.detalle_venta (cantidad, precio_unitario, subtotal, id_venta, id_producto) " +
                                "VALUES (?, ?, ?, ?, ?) RETURNING id_detalle_venta";
            String sqlKardex = "INSERT INTO public.movimiento_inventario " +
                               "(fecha_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, id_tipo_movimiento, " +
                               "id_producto, id_usuario, id_detalle_venta, id_est_movimiento, id_usuario_autorizador, " +
                               "fecha_autorizacion, observacion_autorizacion) " +
                               "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
            String sqlUpdStock = "UPDATE public.producto SET stock_actual = ?, id_est_producto = ? WHERE id_producto = ?";

            for (DetalleVenta det : venta.getDetalles()) {
                int idProd = det.getProducto().getIdProducto();
                int cant = det.getCantidad();
                int stockAnterior;
                int stockReservado;
                String nombreProd;

                try (PreparedStatement psLock = conn.prepareStatement(sqlLockProd)) {
                    psLock.setInt(1, idProd);
                    try (ResultSet rsP = psLock.executeQuery()) {
                        if (!rsP.next()) {
                            throw new SQLException("El producto con ID " + idProd + " no existe.");
                        }
                        stockAnterior = rsP.getInt("stock_actual");
                        stockReservado = rsP.getInt("stock_reservado");
                        nombreProd = rsP.getString("nombre_producto");
                    }
                }

                int disponible = stockAnterior - stockReservado;
                if (disponible < cant) {
                    throw new SQLException("Stock insuficiente para '" + nombreProd + "'. Disponible: " + disponible + ", Solicitado: " + cant);
                }

                int idDetalleGenerado;
                try (PreparedStatement psDet = conn.prepareStatement(sqlDetalle)) {
                    psDet.setInt(1, cant);
                    psDet.setBigDecimal(2, det.getPrecioUnitario());
                    psDet.setBigDecimal(3, det.calcularSubtotal());
                    psDet.setInt(4, venta.getIdVenta());
                    psDet.setInt(5, idProd);
                    try (ResultSet rsD = psDet.executeQuery()) {
                        if (rsD.next()) {
                            idDetalleGenerado = rsD.getInt(1);
                            det.setIdDetalleVenta(idDetalleGenerado);
                            det.setIdVenta(venta.getIdVenta());
                        } else {
                            throw new SQLException("Error al insertar línea de detalle de venta.");
                        }
                    }
                }

                int stockNuevo = stockAnterior - cant;
                try (PreparedStatement psKar = conn.prepareStatement(sqlKardex)) {
                    psKar.setInt(1, cant);
                    psKar.setInt(2, stockAnterior);
                    psKar.setInt(3, stockNuevo);
                    psKar.setString(4, "Comprobante Venta #" + venta.getIdVenta() + " - Detalle #" + idDetalleGenerado);
                    psKar.setInt(5, TipoMovimiento.VENTA_CONFIRMADA.getId());
                    psKar.setInt(6, idProd);
                    psKar.setInt(7, venta.getVendedor().getIdUsuario());
                    psKar.setInt(8, idDetalleGenerado);
                    psKar.setInt(9, EstadoMovimiento.AUTORIZADO.getId());
                    psKar.setInt(10, venta.getVendedor().getIdUsuario());
                    psKar.setString(11, "Autorización automática por Venta Confirmada RF-10/RF-11");
                    psKar.executeUpdate();
                }

                int nuevoEstadoProd = stockNuevo == 0 ? EstadoProducto.AGOTADO.getId() : EstadoProducto.DISPONIBLE.getId();
                try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdStock)) {
                    psUpd.setInt(1, stockNuevo);
                    psUpd.setInt(2, nuevoEstadoProd);
                    psUpd.setInt(3, idProd);
                    psUpd.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }
    }

    public Venta buscarPorId(int idVenta) throws SQLException {
        String sql = "SELECT v.id_venta, v.fecha_venta, v.tipo_venta, v.subtotal, v.descuento, v.impuesto, " +
                     "v.total_venta, v.saldo_pendiente, v.id_est_venta, " +
                     "c.id_cliente, c.nombre AS c_nom, c.apellido AS c_ape, c.documento, " +
                     "u.id_usuario, u.nombre AS u_nom, u.apellido AS u_ape, u.nombre_usuario " +
                     "FROM public.venta v " +
                     "INNER JOIN public.cliente c ON v.id_cliente = c.id_cliente " +
                     "INNER JOIN public.usuario u ON v.id_usuario = u.id_usuario " +
                     "WHERE v.id_venta = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta v = mapearCabecera(rs);
                    v.setDetalles(listarDetallesPorVenta(idVenta));
                    return v;
                }
            }
        }
        return null;
    }

    public List<Venta> listarTodas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.fecha_venta, v.tipo_venta, v.subtotal, v.descuento, v.impuesto, " +
                     "v.total_venta, v.saldo_pendiente, v.id_est_venta, " +
                     "c.id_cliente, c.nombre AS c_nom, c.apellido AS c_ape, c.documento, " +
                     "u.id_usuario, u.nombre AS u_nom, u.apellido AS u_ape, u.nombre_usuario " +
                     "FROM public.venta v " +
                     "INNER JOIN public.cliente c ON v.id_cliente = c.id_cliente " +
                     "INNER JOIN public.usuario u ON v.id_usuario = u.id_usuario " +
                     "ORDER BY v.fecha_venta DESC, v.id_venta DESC";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCabecera(rs));
            }
        }
        return lista;
    }

    public List<DetalleVenta> listarDetallesPorVenta(int idVenta) throws SQLException {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT d.id_detalle_venta, d.cantidad, d.precio_unitario, d.subtotal, d.id_venta, " +
                     "p.id_producto, p.nombre_producto " +
                     "FROM public.detalle_venta d " +
                     "INNER JOIN public.producto p ON d.id_producto = p.id_producto " +
                     "WHERE d.id_venta = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setNombreProducto(rs.getString("nombre_producto"));
                    p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    DetalleVenta dv = new DetalleVenta(
                            rs.getInt("id_detalle_venta"),
                            rs.getInt("id_venta"),
                            p,
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario")
                    );
                    dv.setSubtotal(rs.getBigDecimal("subtotal"));
                    detalles.add(dv);
                }
            }
        }
        return detalles;
    }

    private Venta mapearCabecera(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        Timestamp ts = rs.getTimestamp("fecha_venta");
        if (ts != null) v.setFechaVenta(ts.toLocalDateTime());
        v.setTipoVenta(rs.getString("tipo_venta"));
        v.setSubtotal(rs.getBigDecimal("subtotal"));
        v.setDescuento(rs.getBigDecimal("descuento"));
        v.setImpuesto(rs.getBigDecimal("impuesto"));
        v.setTotalVenta(rs.getBigDecimal("total_venta"));
        v.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        v.setEstado(EstadoVenta.desdeId(rs.getInt("id_est_venta")));

        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("c_nom"));
        c.setApellido(rs.getString("c_ape"));
        c.setDocumento(rs.getString("documento"));
        v.setCliente(c);

        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("u_nom"));
        u.setApellido(rs.getString("u_ape"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        v.setVendedor(u);
        return v;
    }
}
