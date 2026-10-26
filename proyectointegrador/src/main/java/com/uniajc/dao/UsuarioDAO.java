// Autor: Alejandro Mosquera | Commit 3: DAO de Usuario y Rol con PreparedStatement para PostgreSQL
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.EstadoUsuario;
import com.uniajc.modelo.Rol;
import com.uniajc.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario buscarPorNombreUsuario(String username) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.correo, u.telefono, " +
                     "u.nombre_usuario, u.contrasena, u.id_est_usuario, " +
                     "r.id_rol, r.nombre_rol, r.descripcion AS desc_rol " +
                     "FROM public.usuario u " +
                     "INNER JOIN public.rol r ON u.id_rol = r.id_rol " +
                     "WHERE u.nombre_usuario = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    public boolean insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO public.usuario (nombre, apellido, correo, telefono, nombre_usuario, contrasena, id_rol, id_est_usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_usuario";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getTelefono());
            ps.setString(5, u.getNombreUsuario());
            ps.setString(6, u.getContrasena());
            ps.setInt(7, u.getRol().getIdRol());
            ps.setInt(8, u.getEstado().getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setIdUsuario(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    public boolean actualizarEstadoYRol(int idUsuario, int idRol, EstadoUsuario nuevoEstado) throws SQLException {
        String sql = "UPDATE public.usuario SET id_rol = ?, id_est_usuario = ? WHERE id_usuario = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            ps.setInt(2, nuevoEstado.getId());
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.correo, u.telefono, " +
                     "u.nombre_usuario, u.contrasena, u.id_est_usuario, " +
                     "r.id_rol, r.nombre_rol, r.descripcion AS desc_rol " +
                     "FROM public.usuario u " +
                     "INNER JOIN public.rol r ON u.id_rol = r.id_rol " +
                     "ORDER BY u.id_usuario";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        }
        return lista;
    }

    public List<Rol> listarRoles() throws SQLException {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre_rol, descripcion FROM public.rol ORDER BY id_rol";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre_rol"), rs.getString("descripcion")));
            }
        }
        return roles;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("nombre_rol"), rs.getString("desc_rol"));
        EstadoUsuario estado = EstadoUsuario.desdeId(rs.getInt("id_est_usuario"));
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("nombre_usuario"),
                rs.getString("contrasena"),
                rol,
                estado
        );
    }
}
