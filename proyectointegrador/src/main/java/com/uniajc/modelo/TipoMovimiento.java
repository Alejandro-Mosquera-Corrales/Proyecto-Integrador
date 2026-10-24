// Autor: Alejandro Mosquera | Commit 2: Enum TipoMovimiento para los 7 eventos del Kardex
package com.uniajc.modelo;

public enum TipoMovimiento {
    COMPRA_RECIBIDA(1, "Compra recibida", true),
    DEVOLUCION_CLIENTE(2, "Devolucion de cliente", true),
    AJUSTE_POSITIVO_FISICO(3, "Ajuste positivo fisico", true),
    VENTA_CONFIRMADA(4, "Venta confirmada", false),
    MERCANCIA_DANADA(5, "Mercancia danada", false),
    AJUSTE_NEGATIVO_FISICO(6, "Ajuste negativo fisico", false),
    DEVOLUCION_PROVEEDOR(7, "Devolucion a proveedor", false);

    private final int id;
    private final String descripcion;
    private final boolean entrada;

    TipoMovimiento(int id, String descripcion, boolean entrada) {
        this.id = id;
        this.descripcion = descripcion;
        this.entrada = entrada;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public boolean esEntrada() { return entrada; }

    public static TipoMovimiento desdeId(int id) {
        for (TipoMovimiento tm : values()) {
            if (tm.id == id) return tm;
        }
        return COMPRA_RECIBIDA;
    }
}
