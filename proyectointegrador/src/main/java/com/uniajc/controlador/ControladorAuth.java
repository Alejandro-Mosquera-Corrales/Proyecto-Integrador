// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para autenticacion, sesion y administracion de usuarios
package com.uniajc.controlador;

import com.uniajc.modelo.EstadoUsuario;
import com.uniajc.modelo.Rol;
import com.uniajc.modelo.Usuario;
import com.uniajc.servicios.AuthService;

import java.sql.SQLException;
import java.util.List;

public class ControladorAuth {

    private final AuthService authService;

    public ControladorAuth() {
        this(new AuthService());
    }

    public ControladorAuth(AuthService authService) {
        this.authService = authService;
    }

    public Usuario iniciarSesion(String username, String password) throws SQLException {
        return authService.autenticar(username, password);
    }

    public boolean registrarUsuario(String nombre, String apellido, String correo, String telefono,
                                    String username, String password, Rol rol, EstadoUsuario estado) throws SQLException {
        Usuario nuevo = new Usuario(0, nombre, apellido, correo, telefono, username, password, rol,
                estado != null ? estado : EstadoUsuario.ACTIVO);
        return authService.registrarUsuario(nuevo, authService.getUsuarioAutenticado());
    }

    public boolean actualizarRolYEstado(int idUsuario, int idRol, EstadoUsuario nuevoEstado) throws SQLException {
        return authService.actualizarEstadoYRol(idUsuario, idRol, nuevoEstado, authService.getUsuarioAutenticado());
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        return authService.listarUsuarios();
    }

    public List<Rol> listarRoles() throws SQLException {
        return authService.listarRoles();
    }

    public Usuario getUsuarioSesion() {
        return authService.getUsuarioAutenticado();
    }

    public void cerrarSesion() {
        authService.cerrarSesion();
    }
}
