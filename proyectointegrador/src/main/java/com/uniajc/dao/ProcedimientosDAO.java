package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ProcedimientosDAO {

    /**
     * DTO interno para retornar los parámetros de salida de cualquier procedimiento almacenado.
     */
    public static class ResultadoSP {
        private final Integer idGenerado;
        private final BigDecimal nuevoSaldo;
        private final int codigoResultado;
        private final String mensaje;

        public ResultadoSP(Integer idGenerado, BigDecimal nuevoSaldo, int codigoResultado, String mensaje) {
            this.idGenerado = idGenerado;
            this.nuevoSaldo = nuevoSaldo;
            this.codigoResultado = codigoResultado;
            this.mensaje = mensaje;
        }

        public Integer getIdGenerado() { return idGenerado; }
        public BigDecimal getNuevoSaldo() { return nuevoSaldo; }
        public int getCodigoResultado() { return codigoResultado; }
        public String getMensaje() { return mensaje; }
        public boolean isExitoso() { return codigoResultado == 1; }

        @Override
        public String toString() {
            return "ResultadoSP{codigo=" + codigoResultado + ", idGenerado=" + idGenerado
                    + ", nuevoSaldo=" + nuevoSaldo + ", mensaje='" + mensaje + "'}";
        }
    }

    /**
     * RF-10: Invoca public.sp_registrar_venta mediante CallableStatement.
     * @param jsonDetalles Ejemplo: "[{\"id_producto\": 1, \"cantidad\": 2}]"
     */
    public ResultadoSP registrarVenta(int idCliente, int idVendedor, String tipoVenta,
                                      BigDecimal descuento, BigDecimal impuesto, String jsonDetalles) throws SQLException {
        String sql = "CALL public.sp_registrar_venta(?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?)";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();

        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idCliente);
            cs.setInt(2, idVendedor);
            cs.setString(3, tipoVenta);
            cs.setBigDecimal(4, descuento);
            cs.setBigDecimal(5, impuesto);
            cs.setString(6, jsonDetalles);

            // Parámetros INOUT (salida)
            cs.setNull(7, Types.INTEGER);
            cs.registerOutParameter(7, Types.INTEGER);
            cs.setNull(8, Types.INTEGER);
            cs.registerOutParameter(8, Types.INTEGER);
            cs.setNull(9, Types.VARCHAR);
            cs.registerOutParameter(9, Types.VARCHAR);

            cs.execute();

            int idVentaVal = cs.getInt(7);
            Integer idVentaGen = cs.wasNull() ? null : idVentaVal;
            int codigo = cs.getInt(8);
            String mensaje = cs.getString(9);

            return new ResultadoSP(idVentaGen, null, codigo, mensaje);
        }
    }

    /**
     * RF-16: Invoca public.sp_decidir_movimiento_kardex mediante CallableStatement.
     */
    public ResultadoSP decidirMovimientoKardex(int idMovimiento, int idAutorizador,
                                               String decision, String motivoDecision) throws SQLException {
        String sql = "CALL public.sp_decidir_movimiento_kardex(?, ?, ?, ?, ?, ?)";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();

        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idMovimiento);
            cs.setInt(2, idAutorizador);
            cs.setString(3, decision);
            cs.setString(4, motivoDecision);

            // Parámetros INOUT (salida)
            cs.setNull(5, Types.INTEGER);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.setNull(6, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();

            int codigo = cs.getInt(5);
            String mensaje = cs.getString(6);

            return new ResultadoSP(null, null, codigo, mensaje);
        }
    }

    /**
     * RF-18: Invoca public.sp_registrar_pago mediante CallableStatement.
     */
    public ResultadoSP registrarPago(Integer idPago, int idVenta, BigDecimal montoPago,
                                     int idMetodoPago, int idEstadoPago, int idUsuario,
                                     String referenciaPago, String tipoPago) throws SQLException {
        String sql = "CALL public.sp_registrar_pago(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();

        try (CallableStatement cs = conn.prepareCall(sql)) {
            if (idPago == null) {
                cs.setNull(1, Types.INTEGER);
            } else {
                cs.setInt(1, idPago);
            }
            cs.setInt(2, idVenta);
            cs.setBigDecimal(3, montoPago);
            cs.setInt(4, idMetodoPago);
            cs.setInt(5, idEstadoPago);
            cs.setInt(6, idUsuario);
            cs.setString(7, referenciaPago);
            cs.setString(8, tipoPago);

            // Parámetros INOUT (salida)
            cs.setNull(9, Types.NUMERIC);
            cs.registerOutParameter(9, Types.NUMERIC);
            cs.setNull(10, Types.INTEGER);
            cs.registerOutParameter(10, Types.INTEGER);
            cs.setNull(11, Types.VARCHAR);
            cs.registerOutParameter(11, Types.VARCHAR);

            cs.execute();

            BigDecimal nuevoSaldo = cs.getBigDecimal(9);
            int codigo = cs.getInt(10);
            String mensaje = cs.getString(11);

            return new ResultadoSP(null, nuevoSaldo, codigo, mensaje);
        }
    }
}