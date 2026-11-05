// Autor: Jahel Gutiérrez | Commit 7: Pruebas unitarias de Venta, DetalleVenta y cálculo de totales (RF-10)
package com.uniajc.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class VentaTest {

    @Test
    @DisplayName("Cálculo de subtotal de línea en DetalleVenta")
    public void testSubtotalDetalleVenta() {
        Producto p = new Producto();
        p.setIdProducto(1);
        p.setNombreProducto("Mesa de Centro");
        p.setPrecioUnitario(new BigDecimal("250000.00"));

        DetalleVenta det = new DetalleVenta(1, 10, p, 3, new BigDecimal("250000.00"));

        assertEquals(new BigDecimal("750000.00"), det.calcularSubtotal());
    }

    @Test
    @DisplayName("Recálculo automático de totales, impuestos, descuentos y saldo pendiente en Venta")
    public void testRecalculoTotalesVenta() {
        Producto p1 = new Producto();
        p1.setIdProducto(1);
        p1.setNombreProducto("Mesa");
        p1.setPrecioUnitario(new BigDecimal("300000.00"));

        Producto p2 = new Producto();
        p2.setIdProducto(2);
        p2.setNombreProducto("Silla");
        p2.setPrecioUnitario(new BigDecimal("100000.00"));

        Venta venta = new Venta();
        venta.agregarDetalle(p1, 2); // 600,000
        venta.agregarDetalle(p2, 4); // 400,000

        assertEquals(new BigDecimal("1000000.00"), venta.getSubtotal());

        // Aplicar descuento de 50,000 e impuesto de 190,000
        venta.setDescuento(new BigDecimal("50000.00"));
        venta.setImpuesto(new BigDecimal("190000.00"));
        venta.recalcularTotales();

        // Total = (1,000,000 - 50,000) + 190,000 = 1,140,000
        assertEquals(new BigDecimal("1140000.00"), venta.getTotalVenta());
        assertEquals(new BigDecimal("1140000.00"), venta.getSaldoPendiente());
    }
}
