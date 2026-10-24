// Autor: Alejandro Mosquera | Commit 2: Entidad Usuario que extiende Persona y compone Rol
package com.uniajc.modelo;

public class Usuario extends Persona {

    private int idUsuario;
    private String nombreUsuario;
    private String contrasena;
    private Rol rol;
    private EstadoUsuario estado;

    public Usuario() {
        super();
        this.estado = EstadoUsuario.ACTIVO;
    }

    public Usuario(int idUsuario, String nombre, String apellido, String correo, String telefono,
                   String nombreUsuario, String contrasena, Rol rol, EstadoUsuario estado) {
        super(nombre, apellido, correo, telefono);
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = estado;
    }

    public boolean estaActivo() {
        return this.estado == EstadoUsuario.ACTIVO;
    }

    public boolean tienePermiso(String accion, String modulo) {
        if (!estaActivo() || rol == null) return false;
        switch (accion.toUpperCase()) {
            case "CREAR": return rol.puedeCrear(modulo);
            case "CONSULTAR": return rol.puedeConsultar(modulo);
            case "ACTUALIZAR": return rol.puedeActualizar(modulo);
            case "ELIMINAR": return rol.puedeEliminar(modulo);
            default: return false;
        }
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    @Override
    public String toString() {
        return getNombreCompleto() + " [" + nombreUsuario + "]";
    }
}
