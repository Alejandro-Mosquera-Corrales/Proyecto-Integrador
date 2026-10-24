// Autor: Alejandro Mosquera | Commit 2: Enum EstadoProducto mapeado a tabla estado_producto
package com.uniajc.modelo;

public enum EstadoProducto {
    DISPONIBLE(1, "Disponible"),
    AGOTADO(2, "Agotado"),
    INACTIVO(3, "Inactivo");

    private final int id;
    private final String descripcion;

    EstadoProducto(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoProducto desdeId(int id) {
        for (EstadoProducto ep : values()) {
            if (ep.id == id) return ep;
        }
        return DISPONIBLE;
    }
}
