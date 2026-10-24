// Autor: Alejandro Mosquera | Commit 2: Enum EstadoUsuario mapeado a tabla estado_usuario
package com.uniajc.modelo;

public enum EstadoUsuario {
    ACTIVO(1, "Activo"),
    INACTIVO(2, "Inactivo");

    private final int id;
    private final String descripcion;

    EstadoUsuario(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    public static EstadoUsuario desdeId(int id) {
        return id == 1 ? ACTIVO : INACTIVO;
    }
}
