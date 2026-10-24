// Autor: Alejandro Mosquera | Commit 2: Entidad Rol con reglas de permisos por perfil
package com.uniajc.modelo;

public class Rol {

    private int idRol;
    private String nombreRol;
    private String descripcion;

    public Rol() {
    }

    public Rol(int idRol, String nombreRol, String descripcion) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }

    public boolean puedeCrear(String modulo) {
        if (nombreRol == null) return false;
        String r = nombreRol.toUpperCase();
        String m = modulo.toUpperCase();
        if (r.contains("ADMINISTRADOR")) return true;
        if (r.contains("VENDEDOR")) {
            return m.equals("VENTAS") || m.equals("CLIENTES") || m.equals("PAGOS") || m.equals("ENTREGAS");
        }
        if (r.contains("INVENTARIO")) {
            return m.equals("PRODUCTOS") || m.equals("PROVEEDORES") || m.equals("KARDEX");
        }
        return false;
    }

    public boolean puedeConsultar(String modulo) {
        if (nombreRol == null) return false;
        String r = nombreRol.toUpperCase();
        String m = modulo.toUpperCase();
        if (r.contains("ADMINISTRADOR") || r.contains("GERENTE") || r.contains("AUDITOR")) return true;
        if (r.contains("VENDEDOR")) {
            return m.equals("VENTAS") || m.equals("CLIENTES") || m.equals("PRODUCTOS") || m.equals("PAGOS") || m.equals("ENTREGAS");
        }
        if (r.contains("INVENTARIO") || r.contains("AUTORIZADOR")) {
            return m.equals("PRODUCTOS") || m.equals("PROVEEDORES") || m.equals("KARDEX");
        }
        if (r.contains("TRANSPORTADOR")) {
            return m.equals("ENTREGAS");
        }
        return false;
    }

    public boolean puedeActualizar(String modulo) {
        if (nombreRol == null) return false;
        String r = nombreRol.toUpperCase();
        String m = modulo.toUpperCase();
        if (r.contains("ADMINISTRADOR")) return !m.equals("STOCK_DIRECTO");
        if (r.contains("INVENTARIO")) return m.equals("PRODUCTOS") || m.equals("PROVEEDORES");
        if (r.contains("AUTORIZADOR")) return m.equals("KARDEX");
        if (r.contains("TRANSPORTADOR")) return m.equals("ENTREGAS");
        return false;
    }

    public boolean puedeEliminar(String modulo) {
        return nombreRol != null && nombreRol.toUpperCase().contains("ADMINISTRADOR");
    }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() { return nombreRol; }
}
