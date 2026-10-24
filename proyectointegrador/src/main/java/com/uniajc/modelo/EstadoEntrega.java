// Autor: Alejandro Mosquera | Commit 2: Enum EstadoEntrega (RN-07: En bodega, En ruta, Entregado)
package com.uniajc.modelo;

public enum EstadoEntrega {
    EN_BODEGA(1, "En bodega"),
    EN_RUTA(2, "En ruta"),
    ENTREGADO(3, "Entregado");

    private final int id;
    private final String descripcion;

    EstadoEntrega(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoEntrega desdeId(int id) {
        for (EstadoEntrega ee : values()) {
            if (ee.id == id) return ee;
        }
        return EN_BODEGA;
    }
}
