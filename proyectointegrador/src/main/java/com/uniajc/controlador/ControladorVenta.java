// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para gestion de Ventas, Pagos y Comprobantes
package com.uniajc.controlador;

import com.uniajc.modelo.*;
import com.uniajc.servicios.VentaService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ControladorVenta {

    private final VentaService ventaService;

    public ControladorVenta() {
        this(new VentaService());
    }

    public ControladorVenta(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    public boolean registrarVenta(Venta venta) throws SQLException {
        return ventaService.registrarVenta(venta);
    }

    public boolean registrarPago(int idVenta, BigDecimal monto, MetodoPago metodo,
                                 String referencia, String tipoPago, Usuario cajero) throws SQLException {
        Pago pago = new Pago();
        pago.setIdVenta(idVenta);
        pago.setMontoPago(monto);
        pago.setMetodo(metodo);
        pago.setReferenciaPago(referencia);
        pago.setTipoPago(tipoPago);
        pago.setCajero(cajero);
        return ventaService.registrarPago(pago);
    }

    public Venta buscarVentaPorId(int idVenta) throws SQLException {
        return ventaService.buscarVentaPorId(idVenta);
    }

    public List<Venta> listarVentas() throws SQLException {
        return ventaService.listarVentas();
    }

    public List<DetalleVenta> listarDetallesPorVenta(int idVenta) throws SQLException {
        return ventaService.listarDetallesPorVenta(idVenta);
    }

    public List<Pago> listarPagosPorVenta(int idVenta) throws SQLException {
        return ventaService.listarPagosPorVenta(idVenta);
    }

    public String generarComprobanteTexto(Venta venta) {
        return ventaService.generarComprobanteTexto(venta);
    }
}
