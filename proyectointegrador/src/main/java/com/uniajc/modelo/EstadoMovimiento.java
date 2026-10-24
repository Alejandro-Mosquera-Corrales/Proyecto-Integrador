// Autor: Alejandro Mosquera | Commit 2: Enum EstadoMovimiento para autorizaciones del Kardex
package com.uniajc.modelo;

public enum EstadoMovimiento {
    PENDIENTE(1, "Pendiente"),
    AUTORIZADO(2, "Autorizado"),
    RECHAZADO(3, "Rechazado");

    private final int id;
    private final String descripcion;

    EstadoMovimiento(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoMovimiento desdeId(int id) {
        for (EstadoMovimiento em : values()) {
            if (em.id == id) return em;
        }
        return PENDIENTE;
    }
}
