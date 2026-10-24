// Autor: Alejandro Mosquera | Commit 2: Enum EstadoPago mapeado a tabla estado_pago
package com.uniajc.modelo;

public enum EstadoPago {
    CONFIRMADO(1, "Confirmado"),
    PENDIENTE(2, "Pendiente"),
    RECHAZADO(3, "Rechazado");

    private final int id;
    private final String descripcion;

    EstadoPago(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoPago desdeId(int id) {
        for (EstadoPago ep : values()) {
            if (ep.id == id) return ep;
        }
        return CONFIRMADO;
    }
}
