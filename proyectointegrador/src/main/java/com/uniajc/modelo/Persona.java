// Autor: Alejandro Mosquera | Commit 1: Clase base abstracta Persona para jerarquia de herencia
package com.uniajc.modelo;

public abstract class Persona {

    protected String nombre;
    protected String apellido;
    protected String correo;
    protected String telefono;

    public Persona() {
    }

    public Persona(String nombre, String apellido, String correo, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "").trim() + " " + (apellido != null ? apellido : "").trim();
    }

    public String getDatosContacto() {
        return "Tel: " + (telefono != null ? telefono : "Sin registro") + " | Correo: " + (correo != null ? correo : "Sin registro");
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
