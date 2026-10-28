// Autor: Jahel Gutiérrez | Commit 4: Servicio de autenticacion, control de sesion y gestion de usuarios (RF-01, RF-02, RN-09)
package com.uniajc.servicios;

import com.uniajc.dao.UsuarioDAO;
import com.uniajc.modelo.EstadoUsuario;
import com.uniajc.modelo.Rol;
import com.uniajc.modelo.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private Usuario usuarioAutenticado;

    public AuthService() {
        this(new UsuarioDAO());
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida las credenciales del usuario contra PostgreSQL (RF-01).
     * Verifica que el usuario se encuentre ACTIVO (RN-09) y soporta hash SHA-256 (RNF-01)
     * con compatibilidad para contraseñas de prueba pre-cargadas en la base de datos.
     */
    public Usuario autenticar(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre de usuario.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la contraseña.");
        }

        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(username.trim());
        if (usuario == null) {
            throw new IllegalArgumentException("Credenciales inválidas: usuario no encontrado.");
        }

        if (!usuario.estaActivo()) {
            throw new IllegalStateException("Acceso denegado (RN-09): El usuario '" + username + "' se encuentra INACTIVO.");
        }

        String hashIngresado = hashPassword(password);
        String almacenada = usuario.getContrasena() != null ? usuario.getContrasena().trim() : "";

        boolean coincide = almacenada.equalsIgnoreCase(hashIngresado) || almacenada.equals(password);
        if (!coincide) {
            throw new IllegalArgumentException("Credenciales inválidas: contraseña incorrecta.");
        }

        this.usuarioAutenticado = usuario;
        return usuario;
    }

    /**
     * Registra un nuevo usuario en el sistema cifrando su contraseña con SHA-256 (RF-02, RNF-01).
     */
    public boolean registrarUsuario(Usuario nuevo, Usuario ejecutor) throws SQLException {
        validarPermisoAdmin(ejecutor);
        if (nuevo == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
        if (nuevo.getNombre() == null || nuevo.getNombre().trim().isEmpty() ||
            nuevo.getApellido() == null || nuevo.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre y apellido del usuario son obligatorios.");
        }
        if (nuevo.getNombreUsuario() == null || nuevo.getNombreUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario (login) es obligatorio.");
        }
        if (nuevo.getContrasena() == null || nuevo.getContrasena().trim().length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (nuevo.getRol() == null || nuevo.getRol().getIdRol() <= 0) {
            throw new IllegalArgumentException("Debe asignar un rol válido al usuario.");
        }

        Usuario existente = usuarioDAO.buscarPorNombreUsuario(nuevo.getNombreUsuario().trim());
        if (existente != null) {
            throw new IllegalArgumentException("El nombre de usuario '" + nuevo.getNombreUsuario() + "' ya está registrado.");
        }

        nuevo.setNombre(nuevo.getNombre().trim());
        nuevo.setApellido(nuevo.getApellido().trim());
        nuevo.setNombreUsuario(nuevo.getNombreUsuario().trim());
        nuevo.setContrasena(hashPassword(nuevo.getContrasena()));
        if (nuevo.getEstado() == null) {
            nuevo.setEstado(EstadoUsuario.ACTIVO);
        }

        return usuarioDAO.insertar(nuevo);
    }

    /**
     * Actualiza el rol y el estado (ACTIVO/INACTIVO) de un usuario existente (RF-02, RN-09).
     */
    public boolean actualizarEstadoYRol(int idUsuario, int idRol, EstadoUsuario nuevoEstado, Usuario ejecutor) throws SQLException {
        validarPermisoAdmin(ejecutor);
        if (idUsuario <= 0 || idRol <= 0 || nuevoEstado == null) {
            throw new IllegalArgumentException("Datos inválidos para actualizar el usuario.");
        }
        return usuarioDAO.actualizarEstadoYRol(idUsuario, idRol, nuevoEstado);
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public List<Rol> listarRoles() throws SQLException {
        return usuarioDAO.listarRoles();
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public void cerrarSesion() {
        this.usuarioAutenticado = null;
    }

    public static String hashPassword(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256 de contraseña", e);
        }
    }

    private void validarPermisoAdmin(Usuario ejecutor) {
        if (ejecutor != null && ejecutor.getRol() != null) {
            String rol = ejecutor.getRol().getNombreRol();
            if (rol != null && !rol.toUpperCase().contains("ADMIN")) {
                throw new SecurityException("Solo el rol Administrador puede gestionar usuarios y roles (RF-02).");
            }
        }
    }
}
