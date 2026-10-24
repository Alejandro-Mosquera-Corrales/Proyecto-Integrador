// Autor: Alejandro Mosquera | Commit 2: Entidad Proveedor para abastecimiento e inventario
package com.uniajc.modelo;

public class Proveedor {

    private int idProveedor;
    private String nitDocumento;
    private String razonSocial;
    private String telefono;
    private String correo;

    public Proveedor() {
    }

    public Proveedor(int idProveedor, String nitDocumento, String razonSocial, String telefono, String correo) {
        this.idProveedor = idProveedor;
        this.nitDocumento = nitDocumento;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getResumenInfo() {
        return razonSocial + " - NIT: " + nitDocumento;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNitDocumento() { return nitDocumento; }
    public void setNitDocumento(String nitDocumento) { this.nitDocumento = nitDocumento; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() { return getResumenInfo(); }
}
