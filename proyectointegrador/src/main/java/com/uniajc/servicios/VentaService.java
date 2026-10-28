// Autor: Jahel Gutiérrez | Commit 4: Servicio de Ventas, validacion previa de stock, Pagos y Comprobantes (RF-10, RF-11, RF-18, RN-06)
package com.uniajc.servicios;

import com.uniajc.dao.PagoDAO;
import com.uniajc.dao.ProductoDAO;
import com.uniajc.dao.VentaDAO;
import com.uniajc.modelo.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaService {

    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final PagoDAO pagoDAO;

    public VentaService() {
        this(new VentaDAO(), new ProductoDAO(), new PagoDAO());
    }

    public VentaService(VentaDAO ventaDAO, ProductoDAO productoDAO, PagoDAO pagoDAO) {
        this.ventaDAO = ventaDAO;
        this.productoDAO = productoDAO;
        this.pagoDAO = pagoDAO;
    }

    /**
     * Valida las reglas de negocio y el stock disponible en ProductoDAO antes de ejecutar
     * el registro transaccional de la venta y su salida en el Kardex (RF-10, RF-11, RN-06).
     */
    public boolean registrarVenta(Venta venta) throws SQLException {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        if (venta.getCliente() == null || venta.getCliente().getIdCliente() <= 0) {
            throw new IllegalArgumentException("Debe asociar un cliente válido a la venta.");
        }
        if (venta.getVendedor() == null || venta.getVendedor().getIdUsuario() <= 0) {
            throw new IllegalArgumentException("La venta debe contar con un vendedor autenticado.");
        }
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto en el detalle.");
        }

        // Validación preventiva de stock en la capa de Servicio antes de iniciar la transacción (RN-06)
        for (DetalleVenta det : venta.getDetalles()) {
            if (det.getProducto() == null || det.getProducto().getIdProducto() <= 0) {
                throw new IllegalArgumentException("Uno de los ítems de la venta no tiene producto válido.");
            }
            if (det.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero.");
            }

            Producto prodEnBD = productoDAO.buscarPorId(det.getProducto().getIdProducto());
            if (prodEnBD == null) {
                throw new IllegalArgumentException("El producto ID #" + det.getProducto().getIdProducto() + " no existe en la base de datos.");
            }
            if (!prodEnBD.hayStockSuficiente(det.getCantidad())) {
                throw new IllegalStateException("Stock insuficiente para '" + prodEnBD.getNombreProducto()
                        + "'. Disponible: " + prodEnBD.getStockDisponible()
                        + " unidades, Solicitado: " + det.getCantidad() + " unidades (RN-06).");
            }
            // Asegurar precio oficial vigente si no se asignó
            if (det.getPrecioUnitario() == null || det.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                det.setPrecioUnitario(prodEnBD.getPrecioUnitario());
            }
        }

        venta.recalcularTotales();
        if (venta.getEstado() == null) {
            venta.setEstado(EstadoVenta.CONFIRMADA);
        }

        return ventaDAO.registrarVentaTransaccional(venta);
    }

    /**
     * Registra un abono o pago total validando que no supere el saldo pendiente (RF-18, RN-07).
     */
    public boolean registrarPago(Pago pago) throws SQLException {
        if (pago == null || pago.getIdVenta() <= 0) {
            throw new IllegalArgumentException("Debe indicar una venta válida para registrar el pago.");
        }
        if (pago.getMontoPago() == null || pago.getMontoPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");
        }
        if (pago.getMetodo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un método de pago válido.");
        }
        if (pago.getCajero() == null || pago.getCajero().getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El pago requiere un usuario responsable autenticado.");
        }

        Venta ventaActual = ventaDAO.buscarPorId(pago.getIdVenta());
        if (ventaActual == null) {
            throw new IllegalArgumentException("La venta #" + pago.getIdVenta() + " no existe.");
        }
        if (ventaActual.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La venta #" + pago.getIdVenta() + " ya se encuentra pagada en su totalidad.");
        }
        if (pago.getMontoPago().compareTo(ventaActual.getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El monto del abono ($" + pago.getMontoPago()
                    + ") supera el saldo pendiente actual ($" + ventaActual.getSaldoPendiente() + ").");
        }

        if (pago.getTipoPago() == null || pago.getTipoPago().trim().isEmpty()) {
            boolean esTotal = pago.getMontoPago().compareTo(ventaActual.getSaldoPendiente()) == 0;
            pago.setTipoPago(esTotal ? "Pago Total" : "Abono Parcial");
        }
        if (pago.getReferenciaPago() == null || pago.getReferenciaPago().trim().isEmpty()) {
            pago.setReferenciaPago("REC-" + pago.getIdVenta() + "-" + (System.currentTimeMillis() % 100000));
        }

        boolean ok = pagoDAO.registrarPagoTransaccional(pago);
        if (!ok) {
            throw new IllegalStateException("No fue posible aplicar el pago; verifique el saldo pendiente de la venta.");
        }
        return true;
    }

    public Venta buscarVentaPorId(int idVenta) throws SQLException {
        return ventaDAO.buscarPorId(idVenta);
    }

    public List<Venta> listarVentas() throws SQLException {
        return ventaDAO.listarTodas();
    }

    public List<DetalleVenta> listarDetallesPorVenta(int idVenta) throws SQLException {
        return ventaDAO.listarDetallesPorVenta(idVenta);
    }

    public List<Pago> listarPagosPorVenta(int idVenta) throws SQLException {
        return pagoDAO.listarPorVenta(idVenta);
    }

    /**
     * Genera el comprobante detallado de venta listo para mostrar en pantalla o imprimir (RF-11).
     */
    public String generarComprobanteTexto(Venta v) {
        if (v == null) return "Comprobante no disponible.";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("              MOBILIA HAUS - MUEBLES & DISEÑO             \n");
        sb.append("                 COMPROBANTE OFICIAL DE VENTA             \n");
        sb.append("==========================================================\n");
        sb.append("Venta No.      : #").append(v.getIdVenta()).append("\n");
        sb.append("Fecha          : ").append(v.getFechaVenta() != null ? v.getFechaVenta().format(fmt) : "Hoy").append("\n");
        sb.append("Tipo de Venta  : ").append(v.getTipoVenta()).append("\n");
        sb.append("Estado         : ").append(v.getEstado()).append("\n");
        if (v.getCliente() != null) {
            sb.append("Cliente        : ").append(v.getCliente().getNombreCompleto())
              .append(" (Doc: ").append(v.getCliente().getDocumento()).append(")\n");
        }
        if (v.getVendedor() != null) {
            sb.append("Atendido por   : ").append(v.getVendedor().getNombreCompleto()).append("\n");
        }
        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%-26s %5s %12s %12s\n", "PRODUCTO", "CANT", "V.UNIT", "SUBTOTAL"));
        sb.append("----------------------------------------------------------\n");
        if (v.getDetalles() != null) {
            for (DetalleVenta d : v.getDetalles()) {
                String nom = d.getProducto() != null ? d.getProducto().getNombreProducto() : "Prod";
                if (nom.length() > 25) nom = nom.substring(0, 25);
                sb.append(String.format("%-26s %5d %12s %12s\n",
                        nom,
                        d.getCantidad(),
                        "$" + d.getPrecioUnitario(),
                        "$" + d.calcularSubtotal()));
            }
        }
        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%-44s : $%10s\n", "SUBTOTAL", v.getSubtotal()));
        sb.append(String.format("%-44s : $%10s\n", "DESCUENTO", v.getDescuento()));
        sb.append(String.format("%-44s : $%10s\n", "IMPUESTO", v.getImpuesto()));
        sb.append(String.format("%-44s : $%10s\n", "TOTAL VENTA", v.getTotalVenta()));
        sb.append(String.format("%-44s : $%10s\n", "SALDO PENDIENTE", v.getSaldoPendiente()));
        sb.append("==========================================================\n");
        sb.append("  Salida de inventario asentada automáticamente en Kardex \n");
        sb.append("==========================================================\n");
        return sb.toString();
    }
}
