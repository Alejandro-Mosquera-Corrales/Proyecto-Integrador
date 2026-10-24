// Autor: Alejandro Mosquera | Commit 2: Entidad Producto con BigDecimal y control de stock por Kardex
package com.uniajc.modelo;

import java.math.BigDecimal;

public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String descripcion;
    private String categoria;
    private String material;
    private String color;
    private BigDecimal precioUnitario;
    private int stockActual;
    private int stockReservado;
    private int stockMinimo;
    private EstadoProducto estado;

    public Producto() {
        this.precioUnitario = BigDecimal.ZERO;
        this.estado = EstadoProducto.DISPONIBLE;
    }

    public Producto(int idProducto, String nombreProducto, String descripcion, String categoria,
                    String material, String color, BigDecimal precioUnitario, int stockActual,
                    int stockReservado, int stockMinimo, EstadoProducto estado) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.material = material;
        this.color = color;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
        this.stockReservado = stockReservado;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    public int getStockDisponible() {
        return stockActual - stockReservado;
    }

    public boolean hayStockSuficiente(int cantidadSolicitada) {
        return cantidadSolicitada > 0 && getStockDisponible() >= cantidadSolicitada;
    }

    public boolean requiereReposicion() {
        return stockActual <= stockMinimo;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public int getStockReservado() { return stockReservado; }
    public void setStockReservado(int stockReservado) { this.stockReservado = stockReservado; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public EstadoProducto getEstado() { return estado; }
    public void setEstado(EstadoProducto estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombreProducto + " (Disp: " + getStockDisponible() + " | $" + precioUnitario + ")";
    }
}
