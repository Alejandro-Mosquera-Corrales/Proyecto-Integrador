// Autor: Alejandro Mosquera | Commit 3: DAO Transaccional para MovimientoInventario (Kardex estricto)
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAO {

    /**
     * Registra una solicitud de movimiento en estado PENDIENTE (id_est_movimiento = 1).
     * Según RN-03, mientras está pendiente no altera stock_actual.
     */
    public boolean registrarSolicitud(MovimientoInventario mov) throws SQLException {
        String sql = "INSERT INTO public.movimiento_inventario " +
                     "(fecha_movimiento, cantidad, motivo, id_tipo_movimiento, id_producto, id_usuario, " +
                     "id_proveedor, id_detalle_venta, id_est_movimiento) " +
                     "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_movimiento";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mov.getCantidad());
            ps.setString(2, mov.getMotivo());
            ps.setInt(3, mov.getTipo().getId());
            ps.setInt(4, mov.getProductoAfectado().getIdProducto());
            ps.setInt(5, mov.getUsuarioResponsable().getIdUsuario());
            if (mov.getProveedor() != null && mov.getProveedor().getIdProveedor() > 0) {
                ps.setInt(6, mov.getProveedor().getIdProveedor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if (mov.getIdDetalleVenta() != null && mov.getIdDetalleVenta() > 0) {
                ps.setInt(7, mov.getIdDetalleVenta());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, EstadoMovimiento.PENDIENTE.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mov.setIdMovimiento(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Autoriza o rechaza atómicamente un movimiento pendiente en el Kardex (RF-16, RN-01 a RN-05).
     * Usa SELECT ... FOR UPDATE para bloquear la fila de movimiento y producto evitando condiciones de carrera.
     */
    public boolean decidirMovimientoTransaccional(int idMovimiento, int idAutorizador, boolean aprobar, String observacion) throws SQLException {
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            String sqlLockMov = "SELECT id_producto, cantidad, id_tipo_movimiento, id_est_movimiento " +
                                "FROM public.movimiento_inventario WHERE id_movimiento = ? FOR UPDATE";
            int idProducto;
            int cantidad;
            int idTipo;
            int idEstadoActual;

            try (PreparedStatement psMov = conn.prepareStatement(sqlLockMov)) {
                psMov.setInt(1, idMovimiento);
                try (ResultSet rs = psMov.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("El movimiento de Kardex #" + idMovimiento + " no existe.");
                    }
                    idProducto = rs.getInt("id_producto");
                    cantidad = rs.getInt("cantidad");
                    idTipo = rs.getInt("id_tipo_movimiento");
                    idEstadoActual = rs.getInt("id_est_movimiento");
                }
            }

            if (idEstadoActual != EstadoMovimiento.PENDIENTE.getId()) {
                throw new SQLException("El movimiento #" + idMovimiento + " ya fue decidido previamente (RN-05).");
            }

            if (!aprobar) {
                String sqlRechazo = "UPDATE public.movimiento_inventario " +
                                    "SET id_est_movimiento = ?, id_usuario_autorizador = ?, " +
                                    "fecha_autorizacion = CURRENT_TIMESTAMP, observacion_autorizacion = ? " +
                                    "WHERE id_movimiento = ?";
                try (PreparedStatement psRech = conn.prepareStatement(sqlRechazo)) {
                    psRech.setInt(1, EstadoMovimiento.RECHAZADO.getId());
                    psRech.setInt(2, idAutorizador);
                    psRech.setString(3, observacion);
                    psRech.setInt(4, idMovimiento);
                    psRech.executeUpdate();
                }
                conn.commit();
                return true;
            }

            // Si aprueba: bloquear producto y calcular saldos
            String sqlLockProd = "SELECT stock_actual FROM public.producto WHERE id_producto = ? FOR UPDATE";
            int stockAnterior;
            try (PreparedStatement psProd = conn.prepareStatement(sqlLockProd)) {
                psProd.setInt(1, idProducto);
                try (ResultSet rsP = psProd.executeQuery()) {
                    if (!rsP.next()) {
                        throw new SQLException("Producto #" + idProducto + " no encontrado.");
                    }
                    stockAnterior = rsP.getInt("stock_actual");
                }
            }

            TipoMovimiento tipo = TipoMovimiento.desdeId(idTipo);
            int stockNuevo = tipo.esEntrada() ? (stockAnterior + cantidad) : (stockAnterior - cantidad);

            if (stockNuevo < 0) {
                throw new SQLException("Stock insuficiente para autorizar salida. Stock actual: " + stockAnterior + ", requerido: " + cantidad);
            }

            int nuevoEstadoProd = stockNuevo == 0 ? EstadoProducto.AGOTADO.getId() : EstadoProducto.DISPONIBLE.getId();
            String sqlUpdProd = "UPDATE public.producto SET stock_actual = ?, id_est_producto = ? WHERE id_producto = ?";
            try (PreparedStatement psUpdP = conn.prepareStatement(sqlUpdProd)) {
                psUpdP.setInt(1, stockNuevo);
                psUpdP.setInt(2, nuevoEstadoProd);
                psUpdP.setInt(3, idProducto);
                psUpdP.executeUpdate();
            }

            String sqlUpdMov = "UPDATE public.movimiento_inventario " +
                               "SET stock_anterior = ?, stock_nuevo = ?, id_est_movimiento = ?, " +
                               "id_usuario_autorizador = ?, fecha_autorizacion = CURRENT_TIMESTAMP, " +
                               "observacion_autorizacion = ? WHERE id_movimiento = ?";
            try (PreparedStatement psUpdM = conn.prepareStatement(sqlUpdMov)) {
                psUpdM.setInt(1, stockAnterior);
                psUpdM.setInt(2, stockNuevo);
                psUpdM.setInt(3, EstadoMovimiento.AUTORIZADO.getId());
                psUpdM.setInt(4, idAutorizador);
                psUpdM.setString(5, observacion != null ? observacion : "Autorizado en Kardex");
                psUpdM.setInt(6, idMovimiento);
                psUpdM.executeUpdate();
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

    public List<MovimientoInventario> listarHistorialPorProducto(int idProducto) throws SQLException {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT m.id_movimiento, m.fecha_movimiento, m.cantidad, m.stock_anterior, m.stock_nuevo, " +
                     "m.motivo, m.id_tipo_movimiento, m.id_producto, m.id_usuario, m.id_proveedor, " +
                     "m.id_detalle_venta, m.id_est_movimiento, m.id_usuario_autorizador, " +
                     "m.fecha_autorizacion, m.observacion_autorizacion, p.nombre_producto " +
                     "FROM public.movimiento_inventario m " +
                     "INNER JOIN public.producto p ON m.id_producto = p.id_producto " +
                     "WHERE (? = 0 OR m.id_producto = ?) " +
                     "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<MovimientoInventario> listarPendientes() throws SQLException {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT m.id_movimiento, m.fecha_movimiento, m.cantidad, m.stock_anterior, m.stock_nuevo, " +
                     "m.motivo, m.id_tipo_movimiento, m.id_producto, m.id_usuario, m.id_proveedor, " +
                     "m.id_detalle_venta, m.id_est_movimiento, m.id_usuario_autorizador, " +
                     "m.fecha_autorizacion, m.observacion_autorizacion, p.nombre_producto " +
                     "FROM public.movimiento_inventario m " +
                     "INNER JOIN public.producto p ON m.id_producto = p.id_producto " +
                     "WHERE m.id_est_movimiento = ? " +
                     "ORDER BY m.fecha_movimiento ASC";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, EstadoMovimiento.PENDIENTE.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setIdMovimiento(rs.getInt("id_movimiento"));
        Timestamp ts = rs.getTimestamp("fecha_movimiento");
        if (ts != null) m.setFechaMovimiento(ts.toLocalDateTime());
        m.setCantidad(rs.getInt("cantidad"));
        int stAnt = rs.getInt("stock_anterior");
        if (!rs.wasNull()) m.setStockAnterior(stAnt);
        int stNvo = rs.getInt("stock_nuevo");
        if (!rs.wasNull()) m.setStockNuevo(stNvo);
        m.setMotivo(rs.getString("motivo"));
        m.setTipo(TipoMovimiento.desdeId(rs.getInt("id_tipo_movimiento")));

        Producto prod = new Producto();
        prod.setIdProducto(rs.getInt("id_producto"));
        prod.setNombreProducto(rs.getString("nombre_producto"));
        m.setProductoAfectado(prod);

        Usuario resp = new Usuario();
        resp.setIdUsuario(rs.getInt("id_usuario"));
        m.setUsuarioResponsable(resp);

        m.setEstadoMovimiento(EstadoMovimiento.desdeId(rs.getInt("id_est_movimiento")));
        int idAut = rs.getInt("id_usuario_autorizador");
        if (!rs.wasNull()) {
            Usuario aut = new Usuario();
            aut.setIdUsuario(idAut);
            m.setUsuarioAutorizador(aut);
        }
        Timestamp tsAut = rs.getTimestamp("fecha_autorizacion");
        if (tsAut != null) m.setFechaAutorizacion(tsAut.toLocalDateTime());
        m.setObservacionAutorizacion(rs.getString("observacion_autorizacion"));
        return m;
    }
}
