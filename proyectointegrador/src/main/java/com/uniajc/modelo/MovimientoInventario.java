// Autor: Alejandro Mosquera | Commit 2: Entidad MovimientoInventario (Kardex) con soporte y autorizacion
package com.uniajc.modelo;

import java.time.LocalDateTime;

public class MovimientoInventario {

    private int idMovimiento;
    private LocalDateTime fechaMovimiento;
    private int cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private TipoMovimiento tipo;
    private Producto productoAfectado;
    private Usuario usuarioResponsable;
    private Proveedor proveedor;
    private Integer idDetalleVenta;
    private EstadoMovimiento estadoMovimiento;
    private Usuario usuarioAutorizador;
    private LocalDateTime fechaAutorizacion;
    private String observacionAutorizacion;

    public MovimientoInventario() {
        this.fechaMovimiento = LocalDateTime.now();
        this.estadoMovimiento = EstadoMovimiento.PENDIENTE;
    }

    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int idMovimiento) { this.idMovimiento = idMovimiento; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public Integer getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(Integer stockAnterior) { this.stockAnterior = stockAnterior; }

    public Integer getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(Integer stockNuevo) { this.stockNuevo = stockNuevo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public Producto getProductoAfectado() { return productoAfectado; }
    public void setProductoAfectado(Producto productoAfectado) { this.productoAfectado = productoAfectado; }

    public Usuario getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(Usuario usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Integer getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(Integer idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public EstadoMovimiento getEstadoMovimiento() { return estadoMovimiento; }
    public void setEstadoMovimiento(EstadoMovimiento estadoMovimiento) { this.estadoMovimiento = estadoMovimiento; }

    public Usuario getUsuarioAutorizador() { return usuarioAutorizador; }
    public void setUsuarioAutorizador(Usuario usuarioAutorizador) { this.usuarioAutorizador = usuarioAutorizador; }

    public LocalDateTime getFechaAutorizacion() { return fechaAutorizacion; }
    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) { this.fechaAutorizacion = fechaAutorizacion; }

    public String getObservacionAutorizacion() { return observacionAutorizacion; }
    public void setObservacionAutorizacion(String observacionAutorizacion) { this.observacionAutorizacion = observacionAutorizacion; }
}
