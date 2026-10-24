// Autor: Alejandro Mosquera | Commit 2: Entidad Pago para abonos y pagos totales de ventas
package com.uniajc.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {

    private int idPago;
    private LocalDateTime fechaPago;
    private BigDecimal montoPago;
    private String referenciaPago;
    private String tipoPago;
    private int idVenta;
    private MetodoPago metodo;
    private EstadoPago estado;
    private Usuario cajero;

    public Pago() {
        this.fechaPago = LocalDateTime.now();
        this.montoPago = BigDecimal.ZERO;
        this.tipoPago = "Abono";
        this.metodo = MetodoPago.EFECTIVO;
        this.estado = EstadoPago.CONFIRMADO;
    }

    public boolean validarMontoContraSaldo(BigDecimal saldoPendiente) {
        return montoPago != null
                && montoPago.compareTo(BigDecimal.ZERO) > 0
                && saldoPendiente != null
                && montoPago.compareTo(saldoPendiente) <= 0;
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public BigDecimal getMontoPago() { return montoPago; }
    public void setMontoPago(BigDecimal montoPago) { this.montoPago = montoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public MetodoPago getMetodo() { return metodo; }
    public void setMetodo(MetodoPago metodo) { this.metodo = metodo; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public Usuario getCajero() { return cajero; }
    public void setCajero(Usuario cajero) { this.cajero = cajero; }
}
