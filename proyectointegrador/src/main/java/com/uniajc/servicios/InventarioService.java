// Autor: Jahel Gutiérrez | Commit 4: Servicio de Inventario y Kardex con cumplimiento de Regla de Oro (RN-01 a RN-05)
package com.uniajc.servicios;

import com.uniajc.dao.MovimientoInventarioDAO;
import com.uniajc.dao.ProductoDAO;
import com.uniajc.modelo.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {

    private final ProductoDAO productoDAO;
    private final MovimientoInventarioDAO movimientoDAO;

    public InventarioService() {
        this(new ProductoDAO(), new MovimientoInventarioDAO());
    }

    public InventarioService(ProductoDAO productoDAO, MovimientoInventarioDAO movimientoDAO) {
        this.productoDAO = productoDAO;
        this.movimientoDAO = movimientoDAO;
    }

    /**
     * Crea una ficha de producto en el catálogo con stock inicial estrictamente en 0 (RF-05, RN-01).
     */
    public boolean registrarProductoCatalogo(Producto producto, Usuario ejecutor) throws SQLException {
        validarDatosCatalogo(producto);
        producto.setStockActual(0);
        producto.setStockReservado(0);
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.DISPONIBLE);
        }
        return productoDAO.insertarFichaCatalogo(producto);
    }

    /**
     * Actualiza únicamente datos descriptivos, categoría, precio y stock mínimo del catálogo.
     * Jamás altera stock_actual directamente (RN-01).
     */
    public boolean actualizarFichaProducto(Producto producto, Usuario ejecutor) throws SQLException {
        if (producto == null || producto.getIdProducto() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto válido del catálogo.");
        }
        validarDatosCatalogo(producto);
        return productoDAO.actualizarDatosCatalogo(producto);
    }

    /**
     * Registra una solicitud de movimiento en el Kardex en estado PENDIENTE (RF-07 a RF-09, RF-12 a RF-15, RN-02, RN-03).
     * El stock del producto no cambia hasta que un Autorizador de Kardex o Administrador lo apruebe (RF-16).
     */
    public boolean solicitarMovimientoKardex(MovimientoInventario mov, Usuario solicitante) throws SQLException {
        if (mov == null) {
            throw new IllegalArgumentException("El movimiento de Kardex no puede ser nulo.");
        }
        if (mov.getProductoAfectado() == null || mov.getProductoAfectado().getIdProducto() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto válido para el movimiento.");
        }
        if (mov.getTipo() == null) {
            throw new IllegalArgumentException("Debe especificar el tipo de movimiento de Kardex.");
        }
        if (mov.getTipo() == TipoMovimiento.VENTA_CONFIRMADA) {
            throw new IllegalArgumentException("Las salidas por Venta Confirmada se generan automáticamente desde el módulo de Ventas (RF-10).");
        }
        if (mov.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser un entero positivo mayor a cero.");
        }
        if (mov.getMotivo() == null || mov.getMotivo().trim().length() < 4) {
            throw new IllegalArgumentException("Debe indicar el motivo detallado o soporte documental (factura/acta) del movimiento.");
        }

        // Si es salida no comercial, verificar preventivamente que la cantidad tenga sentido frente al stock actual
        Producto prodActual = productoDAO.buscarPorId(mov.getProductoAfectado().getIdProducto());
        if (prodActual == null) {
            throw new IllegalArgumentException("El producto indicado no existe en el catálogo.");
        }
        if (!mov.getTipo().esEntrada() && prodActual.getStockActual() < mov.getCantidad()) {
            throw new IllegalArgumentException("El stock actual (" + prodActual.getStockActual()
                    + ") es menor que la cantidad de salida solicitada (" + mov.getCantidad() + ").");
        }

        if (solicitante != null && solicitante.getIdUsuario() > 0) {
            mov.setUsuarioResponsable(solicitante);
        } else if (mov.getUsuarioResponsable() == null || mov.getUsuarioResponsable().getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El movimiento requiere un usuario responsable autenticado.");
        }

        mov.setEstadoMovimiento(EstadoMovimiento.PENDIENTE);
        return movimientoDAO.registrarSolicitud(mov);
    }

    /**
     * Aprueba o rechaza transaccionalmente un movimiento pendiente en el Kardex (RF-16, RN-01 a RN-05).
     */
    public boolean decidirMovimientoKardex(int idMovimiento, Usuario autorizador, boolean aprobar, String observacion) throws SQLException {
        if (idMovimiento <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un movimiento pendiente válido.");
        }
        if (autorizador == null || autorizador.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Se requiere un usuario autorizador autenticado.");
        }

        if (autorizador.getRol() != null && autorizador.getRol().getNombreRol() != null) {
            String rol = autorizador.getRol().getNombreRol().toUpperCase();
            boolean permitido = rol.contains("ADMIN") || rol.contains("AUTORIZADOR") || rol.contains("GERENTE");
            if (!permitido) {
                throw new SecurityException("Su rol (" + autorizador.getRol().getNombreRol()
                        + ") no tiene permisos para autorizar o rechazar movimientos de Kardex (RF-16, RN-04).");
            }
        }

        if (!aprobar && (observacion == null || observacion.trim().isEmpty())) {
            throw new IllegalArgumentException("Para rechazar un movimiento de Kardex es obligatorio registrar la observación del motivo (RF-16).");
        }

        String obsFinal = (observacion != null && !observacion.trim().isEmpty())
                ? observacion.trim()
                : (aprobar ? "Autorizado conforme a soporte documental" : "Rechazado");

        return movimientoDAO.decidirMovimientoTransaccional(idMovimiento, autorizador.getIdUsuario(), aprobar, obsFinal);
    }

    public Producto buscarProductoPorId(int idProducto) throws SQLException {
        return productoDAO.buscarPorId(idProducto);
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoDAO.listarTodos();
    }

    public List<Producto> buscarProductos(String criterio) throws SQLException {
        return productoDAO.buscarPorCriterio(criterio);
    }

    /**
     * Filtra los productos cuyo stock actual es menor o igual a su stock mínimo (RF-06).
     */
    public List<Producto> listarProductosAlertaStockBajo() throws SQLException {
        List<Producto> todos = productoDAO.listarTodos();
        List<Producto> alertas = new ArrayList<>();
        for (Producto p : todos) {
            if (p.requiereReposicion()) {
                alertas.add(p);
            }
        }
        return alertas;
    }

    public List<MovimientoInventario> listarPendientesKardex() throws SQLException {
        return movimientoDAO.listarPendientes();
    }

    public List<MovimientoInventario> listarHistorialKardex(int idProducto) throws SQLException {
        return movimientoDAO.listarHistorialPorProducto(idProducto);
    }

    private void validarDatosCatalogo(Producto p) {
        if (p == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (p.getNombreProducto() == null || p.getNombreProducto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del mueble/producto es obligatorio.");
        }
        if (p.getCategoria() == null || p.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria.");
        }
        if (p.getPrecioUnitario() == null || p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero.");
        }
        if (p.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
    }
}
