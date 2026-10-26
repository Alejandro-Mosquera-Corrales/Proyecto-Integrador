// Autor: Alejandro Mosquera | Commit 3: DAO de Proveedor con consultas parametrizadas PostgreSQL
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public boolean insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO public.proveedor (nit_documento, razon_social, telefono, correo) " +
                     "VALUES (?, ?, ?, ?) RETURNING id_proveedor";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNitDocumento());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getCorreo());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setIdProveedor(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE public.proveedor SET razon_social = ?, telefono = ?, correo = ? WHERE id_proveedor = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getTelefono());
            ps.setString(3, p.getCorreo());
            ps.setInt(4, p.getIdProveedor());
            return ps.executeUpdate() > 0;
        }
    }

    public Proveedor buscarPorNit(String nit) throws SQLException {
        String sql = "SELECT id_proveedor, nit_documento, razon_social, telefono, correo " +
                     "FROM public.proveedor WHERE nit_documento = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nit);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id_proveedor, nit_documento, razon_social, telefono, correo " +
                     "FROM public.proveedor ORDER BY id_proveedor";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id_proveedor"),
                rs.getString("nit_documento"),
                rs.getString("razon_social"),
                rs.getString("telefono"),
                rs.getString("correo")
        );
    }
}
