// Autor: Alejandro Mosquera | Commit 2: Entidad DetalleVenta con calculo de subtotal BigDecimal
package com.uniajc.modelo;

import java.math.BigDecimal;

public class DetalleVenta {

    private int idDetalleVenta;
    private int idVenta;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleVenta() {
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public DetalleVenta(int idDetalleVenta, int idVenta, Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.idDetalleVenta = idDetalleVenta;
        this.idVenta = idVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || cantidad <= 0) {
            return BigDecimal.ZERO;
        }
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return this.subtotal;
    }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
