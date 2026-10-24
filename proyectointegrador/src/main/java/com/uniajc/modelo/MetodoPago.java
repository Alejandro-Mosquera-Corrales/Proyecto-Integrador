// Autor: Alejandro Mosquera | Commit 2: Enum MetodoPago mapeado a tabla metodo_pago
package com.uniajc.modelo;

public enum MetodoPago {
    EFECTIVO(1, "Efectivo"),
    TARJETA_CREDITO(2, "Tarjeta de Credito"),
    TARJETA_DEBITO(3, "Tarjeta de Debito"),
    TRANSFERENCIA(4, "Transferencia Bancaria");

    private final int id;
    private final String nombreMetodo;

    MetodoPago(int id, String nombreMetodo) {
        this.id = id;
        this.nombreMetodo = nombreMetodo;
    }

    public int getId() { return id; }
    public String getNombreMetodo() { return nombreMetodo; }

    public static MetodoPago desdeId(int id) {
        for (MetodoPago mp : values()) {
            if (mp.id == id) return mp;
        }
        return EFECTIVO;
    }
}
