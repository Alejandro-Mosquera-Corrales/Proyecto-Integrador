// Autor: Alejandro Mosquera | Commit 2: Enum EstadoVenta mapeado a tabla estado_venta
package com.uniajc.modelo;

public enum EstadoVenta {
    CONFIRMADA(1, "Confirmada"),
    PAGADA(2, "Pagada"),
    ANULADA(3, "Anulada");

    private final int id;
    private final String descripcion;

    EstadoVenta(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoVenta desdeId(int id) {
        for (EstadoVenta ev : values()) {
            if (ev.id == id) return ev;
        }
        return CONFIRMADA;
    }
}
