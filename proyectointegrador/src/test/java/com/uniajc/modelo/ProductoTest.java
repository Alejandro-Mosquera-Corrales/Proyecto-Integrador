// Autor: Jahel Gutiérrez | Commit 7: Pruebas unitarias de la entidad Producto y Regla de Oro (RN-01, RF-06)
package com.uniajc.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductoTest {

    @Test
    @DisplayName("El stock disponible debe ser exactamente stockActual menos stockReservado")
    public void testStockDisponible() {
        Producto p = new Producto(1, "Mesa Comedor Roble", "Mesa 6 puestos", "Comedor",
                "Madera", "Nogal", new BigDecimal("850000.00"), 10, 3, 2, EstadoProducto.DISPONIBLE);

        assertEquals(7, p.getStockDisponible(), "El stock disponible debería ser 10 - 3 = 7");
    }

    @Test
    @DisplayName("Debe verificar correctamente suficiencia de stock según cantidad solicitada")
    public void testHayStockSuficiente() {
        Producto p = new Producto(2, "Silla Ergonomica", "Silla oficina", "Oficina",
                "Malla", "Negro", new BigDecimal("320000.00"), 5, 1, 2, EstadoProducto.DISPONIBLE);

        // Disponible = 4
        assertTrue(p.hayStockSuficiente(3), "Debe haber stock suficiente para 3 unidades");
        assertTrue(p.hayStockSuficiente(4), "Debe haber stock suficiente para 4 unidades");
        assertFalse(p.hayStockSuficiente(5), "No debe haber stock suficiente para 5 unidades");
        assertFalse(p.hayStockSuficiente(0), "Cantidad 0 o negativa no debe ser suficiente");
        assertFalse(p.hayStockSuficiente(-1), "Cantidad negativa no debe ser suficiente");
    }

    @Test
    @DisplayName("Alerta de stock bajo cuando stockActual es menor o igual al stockMinimo (RF-06)")
    public void testRequiereReposicion() {
        Producto p = new Producto(3, "Sofa 3 Puestos", "Sofa cuero", "Sala",
                "Cuero", "Cafe", new BigDecimal("1500000.00"), 2, 0, 3, EstadoProducto.DISPONIBLE);

        assertTrue(p.requiereReposicion(), "Debe requerir reposición si stockActual (2) <= stockMinimo (3)");

        p.setStockActual(3);
        assertTrue(p.requiereReposicion(), "Debe requerir reposición si stockActual (3) == stockMinimo (3)");

        p.setStockActual(5);
        assertFalse(p.requiereReposicion(), "No debe requerir reposición si stockActual (5) > stockMinimo (3)");
    }
}
