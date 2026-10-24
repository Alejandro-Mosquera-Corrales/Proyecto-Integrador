// Autor: Alejandro Mosquera | Commit 2: Entidad pasiva Cliente que hereda de Persona
package com.uniajc.modelo;

public class Cliente extends Persona {

    private int idCliente;
    private String documento;
    private String direccion;

    public Cliente() {
        super();
    }

    public Cliente(int idCliente, String nombre, String apellido, String documento,
                   String telefono, String correo, String direccion) {
        super(nombre, apellido, correo, telefono);
        this.idCliente = idCliente;
        this.documento = documento;
        this.direccion = direccion;
    }

    public boolean validarDocumento() {
        return documento != null && documento.trim().length() >= 5;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return getNombreCompleto() + " (Doc: " + documento + ")";
    }
}
