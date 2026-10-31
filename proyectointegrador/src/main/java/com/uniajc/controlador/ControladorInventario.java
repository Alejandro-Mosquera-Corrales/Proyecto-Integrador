// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para Catalogo de Productos y Movimientos de Kardex
package com.uniajc.controlador;

import com.uniajc.modelo.*;
import com.uniajc.servicios.InventarioService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ControladorInventario {

    private final InventarioService inventarioService;

    public ControladorInventario() {
        this(new InventarioService());
    }

    public ControladorInventario(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    public boolean crearProductoCatalogo(String nombre, String descripcion, String categoria,
                                         String material, String color, BigDecimal precioUnitario,
                                         int stockMinimo, Usuario ejecutor) throws SQLException {
        Producto p = new Producto(0, nombre, descripcion, categoria, material, color,
                precioUnitario, 0, 0, stockMinimo, EstadoProducto.DISPONIBLE);
        return inventarioService.registrarProductoCatalogo(p, ejecutor);
    }

    public boolean actualizarProductoCatalogo(int idProducto, String nombre, String descripcion, String categoria,
                                              String material, String color, BigDecimal precioUnitario,
                                              int stockMinimo, EstadoProducto estado, Usuario ejecutor) throws SQLException {
        Producto p = new Producto(idProducto, nombre, descripcion, categoria, material, color,
                precioUnitario, 0, 0, stockMinimo, estado != null ? estado : EstadoProducto.DISPONIBLE);
        return inventarioService.actualizarFichaProducto(p, ejecutor);
    }

    public boolean solicitarMovimientoKardex(Producto producto, TipoMovimiento tipo, int cantidad,
                                             String motivo, Proveedor proveedor, Usuario solicitante) throws SQLException {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProductoAfectado(producto);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(motivo);
        mov.setProveedor(proveedor);
        mov.setUsuarioResponsable(solicitante);
        return inventarioService.solicitarMovimientoKardex(mov, solicitante);
    }

    public boolean decidirMovimientoKardex(int idMovimiento, Usuario autorizador, boolean aprobar, String observacion) throws SQLException {
        return inventarioService.decidirMovimientoKardex(idMovimiento, autorizador, aprobar, observacion);
    }

    public Producto buscarProductoPorId(int idProducto) throws SQLException {
        return inventarioService.buscarProductoPorId(idProducto);
    }

    public List<Producto> listarProductos() throws SQLException {
        return inventarioService.listarProductos();
    }

    public List<Producto> buscarProductos(String criterio) throws SQLException {
        return inventarioService.buscarProductos(criterio);
    }

    public List<Producto> listarProductosAlertaStockBajo() throws SQLException {
        return inventarioService.listarProductosAlertaStockBajo();
    }

    public List<MovimientoInventario> listarPendientesKardex() throws SQLException {
        return inventarioService.listarPendientesKardex();
    }

    public List<MovimientoInventario> listarHistorialKardex(int idProducto) throws SQLException {
        return inventarioService.listarHistorialKardex(idProducto);
    }
}
