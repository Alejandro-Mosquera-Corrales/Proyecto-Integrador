// Autor: Alejandro Mosquera | Commit 3: DAO de Entrega para asignacion y trazabilidad de despachos (RF-19 a RF-21)
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.Entrega;
import com.uniajc.modelo.EstadoEntrega;
import com.uniajc.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntregaDAO {

    public boolean insertar(Entrega e) throws SQLException {
        String sql = "INSERT INTO public.entrega " +
                     "(direccion_entrega, nombre_recibe, documento_recibe, telefono_recibe, " +
                     "fecha_programada, observacion, id_venta, id_usuario, id_est_entrega) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_entrega";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getDireccionEntrega());
            ps.setString(2, e.getNombreRecibe());
            ps.setString(3, e.getDocumentoRecibe());
            ps.setString(4, e.getTelefonoRecibe());
            ps.setTimestamp(5, e.getFechaProgramada() != null ? Timestamp.valueOf(e.getFechaProgramada()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(6, e.getObservacion());
            ps.setInt(7, e.getIdVenta());
            ps.setInt(8, e.getTransportador().getIdUsuario());
            ps.setInt(9, EstadoEntrega.EN_BODEGA.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setIdEntrega(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Actualiza el estado logístico validando transportador asignado y estado anterior (RF-21, RN-08).
     */
    public boolean actualizarEstado(int idEntrega, int idTransportador, EstadoEntrega estadoAnterior, EstadoEntrega nuevoEstado, String observacion) throws SQLException {
        String campoFechaExtra = "";
        if (nuevoEstado == EstadoEntrega.EN_RUTA) {
            campoFechaExtra = ", fecha_salida_bodega = CURRENT_TIMESTAMP ";
        } else if (nuevoEstado == EstadoEntrega.ENTREGADO) {
            campoFechaExtra = ", fecha_entrega = CURRENT_TIMESTAMP ";
        }

        String sql = "UPDATE public.entrega SET id_est_entrega = ?, observacion = COALESCE(?, observacion) " +
                     campoFechaExtra +
                     "WHERE id_entrega = ? AND id_usuario = ? AND id_est_entrega = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado.getId());
            ps.setString(2, observacion);
            ps.setInt(3, idEntrega);
            ps.setInt(4, idTransportador);
            ps.setInt(5, estadoAnterior.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Entrega> listarPorTransportador(int idTransportador) throws SQLException {
        List<Entrega> lista = new ArrayList<>();
        String sql = "SELECT e.id_entrega, e.direccion_entrega, e.nombre_recibe, e.documento_recibe, " +
                     "e.telefono_recibe, e.fecha_programada, e.fecha_salida_bodega, e.fecha_entrega, " +
                     "e.observacion, e.id_venta, e.id_usuario, e.id_est_entrega, u.nombre, u.apellido " +
                     "FROM public.entrega e " +
                     "INNER JOIN public.usuario u ON e.id_usuario = u.id_usuario " +
                     "WHERE (? = 0 OR e.id_usuario = ?) " +
                     "ORDER BY e.fecha_programada ASC, e.id_entrega DESC";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransportador);
            ps.setInt(2, idTransportador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Entrega buscarPorId(int idEntrega) throws SQLException {
        String sql = "SELECT e.id_entrega, e.direccion_entrega, e.nombre_recibe, e.documento_recibe, " +
                     "e.telefono_recibe, e.fecha_programada, e.fecha_salida_bodega, e.fecha_entrega, " +
                     "e.observacion, e.id_venta, e.id_usuario, e.id_est_entrega, u.nombre, u.apellido " +
                     "FROM public.entrega e " +
                     "INNER JOIN public.usuario u ON e.id_usuario = u.id_usuario " +
                     "WHERE e.id_entrega = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEntrega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Entrega mapear(ResultSet rs) throws SQLException {
        Entrega e = new Entrega();
        e.setIdEntrega(rs.getInt("id_entrega"));
        e.setDireccionEntrega(rs.getString("direccion_entrega"));
        e.setNombreRecibe(rs.getString("nombre_recibe"));
        e.setDocumentoRecibe(rs.getString("documento_recibe"));
        e.setTelefonoRecibe(rs.getString("telefono_recibe"));
        Timestamp fp = rs.getTimestamp("fecha_programada");
        if (fp != null) e.setFechaProgramada(fp.toLocalDateTime());
        Timestamp fs = rs.getTimestamp("fecha_salida_bodega");
        if (fs != null) e.setFechaSalidaBodega(fs.toLocalDateTime());
        Timestamp fe = rs.getTimestamp("fecha_entrega");
        if (fe != null) e.setFechaEntrega(fe.toLocalDateTime());
        e.setObservacion(rs.getString("observacion"));
        e.setIdVenta(rs.getInt("id_venta"));

        Usuario t = new Usuario();
        t.setIdUsuario(rs.getInt("id_usuario"));
        t.setNombre(rs.getString("nombre"));
        t.setApellido(rs.getString("apellido"));
        e.setTransportador(t);

        e.setEstado(EstadoEntrega.desdeId(rs.getInt("id_est_entrega")));
        return e;
    }
}
