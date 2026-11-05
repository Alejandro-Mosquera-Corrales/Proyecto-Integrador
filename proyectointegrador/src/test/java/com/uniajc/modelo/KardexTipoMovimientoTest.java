// Autor: Jahel Gutiérrez | Commit 7: Pruebas unitarias de TipoMovimiento y reglas del Kardex (RF-07 a RF-15)
package com.uniajc.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KardexTipoMovimientoTest {

    @Test
    @DisplayName("Clasificación estricta de entradas y salidas en los 7 eventos del Kardex")
    public void testClasificacionEntradasSalidas() {
        // Entradas (aumentan existencias)
        assertTrue(TipoMovimiento.COMPRA_RECIBIDA.esEntrada());
        assertTrue(TipoMovimiento.DEVOLUCION_CLIENTE.esEntrada());
        assertTrue(TipoMovimiento.AJUSTE_POSITIVO_FISICO.esEntrada());

        // Salidas (disminuyen existencias)
        assertFalse(TipoMovimiento.VENTA_CONFIRMADA.esEntrada());
        assertFalse(TipoMovimiento.MERCANCIA_DANADA.esEntrada());
        assertFalse(TipoMovimiento.AJUSTE_NEGATIVO_FISICO.esEntrada());
        assertFalse(TipoMovimiento.DEVOLUCION_PROVEEDOR.esEntrada());
    }

    @Test
    @DisplayName("Mapeo seguro por ID de tipo de movimiento")
    public void testDesdeId() {
        assertEquals(TipoMovimiento.COMPRA_RECIBIDA, TipoMovimiento.desdeId(1));
        assertEquals(TipoMovimiento.VENTA_CONFIRMADA, TipoMovimiento.desdeId(4));
        assertEquals(TipoMovimiento.DEVOLUCION_PROVEEDOR, TipoMovimiento.desdeId(7));
    }
}
