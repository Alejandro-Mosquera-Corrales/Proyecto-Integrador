// Autor: Alejandro Mosquera | Commit 3: DAO Transaccional de Pago usando CTE atomica en PostgreSQL (RF-18)
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.EstadoPago;
import com.uniajc.modelo.MetodoPago;
import com.uniajc.modelo.Pago;
import com.uniajc.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    /**
     * Registra un abono o pago total y descuenta el saldo_pendiente de la venta en una sola sentencia CTE
     * conforme a la sección 2.15 (RF-18) del documento de Bases de Datos II.
     */
    public boolean registrarPagoTransaccional(Pago pago) throws SQLException {
        String sql = "WITH saldo AS ( " +
                     "  UPDATE public.venta " +
                     "  SET saldo_pendiente = saldo_pendiente - ? " +
                     "  WHERE id_venta = ? AND ? > 0 AND saldo_pendiente >= ? " +
                     "  RETURNING id_venta " +
                     ") " +
                     "INSERT INTO public.pago " +
                     "(monto_pago, referencia_pago, tipo_pago, id_venta, id_metodo_pago, id_est_pago, id_usuario) " +
                     "SELECT ?, ?, ?, s.id_venta, ?, ?, ? " +
                     "FROM saldo s RETURNING id_pago";

        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, pago.getMontoPago());
            ps.setInt(2, pago.getIdVenta());
            ps.setBigDecimal(3, pago.getMontoPago());
            ps.setBigDecimal(4, pago.getMontoPago());
            ps.setBigDecimal(5, pago.getMontoPago());
            ps.setString(6, pago.getReferenciaPago());
            ps.setString(7, pago.getTipoPago() != null ? pago.getTipoPago() : "Abono");
            ps.setInt(8, pago.getMetodo().getId());
            ps.setInt(9, EstadoPago.CONFIRMADO.getId());
            ps.setInt(10, pago.getCajero().getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pago.setIdPago(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    public List<Pago> listarPorVenta(int idVenta) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT id_pago, fecha_pago, monto_pago, referencia_pago, tipo_pago, " +
                     "id_venta, id_metodo_pago, id_est_pago, id_usuario " +
                     "FROM public.pago WHERE id_venta = ? ORDER BY fecha_pago DESC";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setIdPago(rs.getInt("id_pago"));
                    Timestamp ts = rs.getTimestamp("fecha_pago");
                    if (ts != null) p.setFechaPago(ts.toLocalDateTime());
                    p.setMontoPago(rs.getBigDecimal("monto_pago"));
                    p.setReferenciaPago(rs.getString("referencia_pago"));
                    p.setTipoPago(rs.getString("tipo_pago"));
                    p.setIdVenta(rs.getInt("id_venta"));
                    p.setMetodo(MetodoPago.desdeId(rs.getInt("id_metodo_pago")));
                    p.setEstado(EstadoPago.desdeId(rs.getInt("id_est_pago")));
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    p.setCajero(u);
                    lista.add(p);
                }
            }
        }
        return lista;
    }
}
