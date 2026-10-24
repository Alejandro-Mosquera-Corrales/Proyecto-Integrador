// Autor: Alejandro Mosquera | Commit 2: Entidad Venta compuesta por List<DetalleVenta>, Cliente y Vendedor
package com.uniajc.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private int idVenta;
    private LocalDateTime fechaVenta;
    private String tipoVenta;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuesto;
    private BigDecimal totalVenta;
    private BigDecimal saldoPendiente;
    private Cliente cliente;
    private Usuario vendedor;
    private EstadoVenta estado;
    private List<DetalleVenta> detalles;

    public Venta() {
        this.fechaVenta = LocalDateTime.now();
        this.tipoVenta = "Contado";
        this.subtotal = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.impuesto = BigDecimal.ZERO;
        this.totalVenta = BigDecimal.ZERO;
        this.saldoPendiente = BigDecimal.ZERO;
        this.estado = EstadoVenta.CONFIRMADA;
        this.detalles = new ArrayList<>();
    }

    public boolean agregarDetalle(Producto prod, int cantidad) {
        if (prod == null || cantidad <= 0) return false;
        DetalleVenta dv = new DetalleVenta(0, this.idVenta, prod, cantidad, prod.getPrecioUnitario());
        this.detalles.add(dv);
        recalcularTotales();
        return true;
    }

    public void recalcularTotales() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            suma = suma.add(d.calcularSubtotal());
        }
        this.subtotal = suma;
        BigDecimal base = this.subtotal.subtract(this.descuento != null ? this.descuento : BigDecimal.ZERO);
        if (base.compareTo(BigDecimal.ZERO) < 0) base = BigDecimal.ZERO;
        this.totalVenta = base.add(this.impuesto != null ? this.impuesto : BigDecimal.ZERO);
        this.saldoPendiente = this.totalVenta;
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getTipoVenta() { return tipoVenta; }
    public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }

    public BigDecimal getTotalVenta() { return totalVenta; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
