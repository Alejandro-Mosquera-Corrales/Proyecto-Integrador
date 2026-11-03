// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Ventas, Detalle, Pagos atomicos y Comprobante (RF-10, RF-11, RF-18, RN-06)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorCliente;
import com.uniajc.controlador.ControladorInventario;
import com.uniajc.controlador.ControladorVenta;
import com.uniajc.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentanaVentasGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorVenta ctrlVenta;
    private final ControladorCliente ctrlCliente;
    private final ControladorInventario ctrlInventario;

    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JTabbedPane tabsIzquierda;

    private JComboBox<Cliente> cmbCliente;
    private JComboBox<String> cmbTipoVenta;
    private JComboBox<Producto> cmbProducto;
    private JTextField txtCantidadProd;
    private JTextField txtDescuento;
    private JTextField txtImpuesto;
    private JLabel lblTotalCarrito;

    private JTextField txtMontoPago;
    private JComboBox<MetodoPago> cmbMetodoPago;
    private JTextField txtReferenciaPago;
    private JLabel lblSeleccion;

    private JButton btnAgregarItem;
    private JButton btnConfirmarVenta;
    private JButton btnRegistrarPago;
    private JButton btnVerComprobante;

    private final List<DetalleVenta> itemsCarrito = new ArrayList<>();
    private int idVentaSeleccionada = 0;

    public VentanaVentasGUI(Usuario usuario, ControladorVenta ctrlVenta,
                            ControladorCliente ctrlCliente, ControladorInventario ctrlInventario) {
        this.usuario = usuario;
        this.ctrlVenta = ctrlVenta;
        this.ctrlCliente = ctrlCliente;
        this.ctrlInventario = ctrlInventario;
        initComponents();
        aplicarPermisos();
        recargarTodo();
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        boolean puedeVender = r.contains("ADMIN") || r.contains("VENDEDOR");
        btnAgregarItem.setEnabled(puedeVender);
        btnConfirmarVenta.setEnabled(puedeVender);
        btnRegistrarPago.setEnabled(puedeVender);
    }

    private void recargarTodo() {
        cargarCombos();
        cargarVentas();
        actualizarTablaCarrito();
    }

    private void cargarCombos() {
        try {
            cmbCliente.removeAllItems();
            for (Cliente c : ctrlCliente.listarClientes()) {
                cmbCliente.addItem(c);
            }
            cmbProducto.removeAllItems();
            for (Producto p : ctrlInventario.listarProductos()) {
                cmbProducto.addItem(p);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes/productos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarVentas() {
        try {
            List<Venta> ventas = ctrlVenta.listarVentas();
            modeloVentas.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Venta v : ventas) {
                modeloVentas.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getFechaVenta() != null ? v.getFechaVenta().format(fmt) : "",
                    v.getCliente() != null ? v.getCliente().getNombreCompleto() : "",
                    v.getTipoVenta(),
                    v.getSubtotal(),
                    v.getDescuento(),
                    v.getImpuesto(),
                    v.getTotalVenta(),
                    v.getSaldoPendiente(),
                    v.getEstado(),
                    v.getVendedor() != null ? v.getVendedor().getNombreCompleto() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al listar ventas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarItemAlCarrito() {
        Producto prod = (Producto) cmbProducto.getSelectedItem();
        if (prod == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto del catálogo.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int cant = Integer.parseInt(txtCantidadProd.getText().trim());
            if (cant <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!prod.hayStockSuficiente(cant)) {
                JOptionPane.showMessageDialog(this,
                        "Stock insuficiente para '" + prod.getNombreProducto() + "'.\n"
                        + "Disponible actual: " + prod.getStockDisponible() + " unidades (RN-06).",
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DetalleVenta dv = new DetalleVenta(0, 0, prod, cant, prod.getPrecioUnitario());
            itemsCarrito.add(dv);
            txtCantidadProd.setText("1");
            tabsIzquierda.setSelectedIndex(1);
            actualizarTablaCarrito();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarTablaCarrito() {
        modeloCarrito.setRowCount(0);
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleVenta d : itemsCarrito) {
            BigDecimal sub = d.calcularSubtotal();
            suma = suma.add(sub);
            modeloCarrito.addRow(new Object[]{
                d.getProducto().getIdProducto(),
                d.getProducto().getNombreProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                sub
            });
        }
        lblTotalCarrito.setText("Subtotal Carrito: $" + suma + " (" + itemsCarrito.size() + " ítems)");
    }

    private void confirmarVenta() {
        if (itemsCarrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto al carrito antes de confirmar la venta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente cli = (Cliente) cmbCliente.getSelectedItem();
        if (cli == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para la venta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal desc = txtDescuento.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtDescuento.getText().trim());
            BigDecimal imp = txtImpuesto.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtImpuesto.getText().trim());

            Venta nueva = new Venta();
            nueva.setCliente(cli);
            nueva.setVendedor(usuario);
            nueva.setTipoVenta(String.valueOf(cmbTipoVenta.getSelectedItem()));
            nueva.setDescuento(desc);
            nueva.setImpuesto(imp);
            nueva.setDetalles(new ArrayList<>(itemsCarrito));
            nueva.recalcularTotales();

            boolean ok = ctrlVenta.registrarVenta(nueva);
            if (ok) {
                itemsCarrito.clear();
                tabsIzquierda.setSelectedIndex(0);
                recargarTodo();
                mostrarDialogoComprobante(nueva);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Descuento e Impuesto deben ser valores numéricos válidos.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo registrar la venta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPagoAbono() {
        if (idVentaSeleccionada <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta en la tabla de Historial de Ventas.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal monto = new BigDecimal(txtMontoPago.getText().trim());
            MetodoPago metodo = (MetodoPago) cmbMetodoPago.getSelectedItem();
            String ref = txtReferenciaPago.getText().trim();

            boolean ok = ctrlVenta.registrarPago(idVentaSeleccionada, monto, metodo, ref, "Abono/Pago", usuario);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Pago por $" + monto + " aplicado exitosamente a la Venta #" + idVentaSeleccionada + " (RF-18).\n"
                        + "El saldo pendiente se actualizó de forma atómica en PostgreSQL.");
                txtMontoPago.setText("");
                txtReferenciaPago.setText("");
                cargarVentas();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto de pago numérico válido.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al registrar pago", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verComprobanteSeleccionado() {
        if (idVentaSeleccionada <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta de la tabla para ver su comprobante (RF-11).", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Venta v = ctrlVenta.buscarVentaPorId(idVentaSeleccionada);
            mostrarDialogoComprobante(v);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoComprobante(Venta v) {
        String texto = ctrlVenta.generarComprobanteTexto(v);
        JTextArea area = new JTextArea(texto, 22, 60);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Comprobante de Venta #" + (v != null ? v.getIdVenta() : ""),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Ventas, Pagos y Comprobantes");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        JPanel panelFondo = new JPanel(new BorderLayout(0, 10));
        panelFondo.setBackground(EstiloGUI.FONDO);

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(EstiloGUI.CAFE_OSCURO);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel panelMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelMarca.setOpaque(false);
        JLabel lblLogo = new JLabel();
        EstiloGUI.ponerLogo(lblLogo, 48, 48);
        JLabel lblMarca = new JLabel("Mobilia Haus");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMarca.setForeground(Color.WHITE);
        panelMarca.add(lblLogo);
        panelMarca.add(lblMarca);

        String rolStr = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "Sin rol";
        JLabel lblUser = new JLabel("Usuario: " + usuario.getNombreCompleto() + " | Rol: " + rolStr);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        panelHeader.add(panelMarca, BorderLayout.WEST);
        panelHeader.add(lblUser, BorderLayout.EAST);
        panelFondo.add(panelHeader, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(14, 10));
        panelCentro.setBackground(EstiloGUI.FONDO);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        JLabel lblTitulo = new JLabel("Gestión de Ventas, Carrito y Abonos (RF-10, RF-11, RF-18)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);
        panelCentro.add(lblTitulo, BorderLayout.NORTH);

        tabsIzquierda = new JTabbedPane();
        tabsIzquierda.setFont(new Font("Segoe UI", Font.BOLD, 13));

        String[] colsV = {"ID Venta", "Fecha", "Cliente", "Tipo", "Subtotal", "Descuento", "Impuesto", "Total", "Saldo Pendiente", "Estado", "Vendedor"};
        modeloVentas = new DefaultTableModel(colsV, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaVentas = new JTable(modeloVentas);
        EstiloGUI.configurarTabla(tablaVentas);
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVentas.getSelectedRow() >= 0) {
                int mf = tablaVentas.convertRowIndexToModel(tablaVentas.getSelectedRow());
                idVentaSeleccionada = (int) modeloVentas.getValueAt(mf, 0);
                Object saldo = modeloVentas.getValueAt(mf, 8);
                txtMontoPago.setText(String.valueOf(saldo));
                lblSeleccion.setText("Venta seleccionada: #" + idVentaSeleccionada + " | Saldo pendiente: $" + saldo);
            }
        });
        tabsIzquierda.addTab("Historial de Ventas Registradas", new JScrollPane(tablaVentas));

        String[] colsC = {"ID Prod", "Producto", "Cantidad", "Precio Unit.", "Subtotal Línea"};
        modeloCarrito = new DefaultTableModel(colsC, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        EstiloGUI.configurarTabla(tablaCarrito);
        tabsIzquierda.addTab("Detalle de Venta en Construcción (Carrito)", new JScrollPane(tablaCarrito));

        // Panel Derecho
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(EstiloGUI.BLANCO);
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Nueva Venta y Registro de Pagos"));
        panelDerecho.setPreferredSize(new Dimension(350, 560));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 8, 2, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        cmbCliente = new JComboBox<>();
        cmbTipoVenta = new JComboBox<>(new String[]{"Contado", "Crédito"});
        cmbProducto = new JComboBox<>();
        txtCantidadProd = new JTextField("1");
        txtDescuento = new JTextField("0");
        txtImpuesto = new JTextField("0");

        y = agregarCampo(panelDerecho, gbc, y, "Cliente *", cmbCliente);
        y = agregarCampo(panelDerecho, gbc, y, "Modalidad de Venta", cmbTipoVenta);
        y = agregarCampo(panelDerecho, gbc, y, "Producto a Agregar *", cmbProducto);
        y = agregarCampo(panelDerecho, gbc, y, "Cantidad *", txtCantidadProd);

        JPanel panelBtnItems = new JPanel(new GridLayout(1, 2, 6, 0));
        panelBtnItems.setOpaque(false);
        btnAgregarItem = new JButton("+ Agregar Ítem");
        JButton btnVaciar = new JButton("Vaciar Carrito");
        EstiloGUI.prepararBoton(btnAgregarItem, false);
        EstiloGUI.prepararBoton(btnVaciar, false);
        btnAgregarItem.addActionListener(e -> agregarItemAlCarrito());
        btnVaciar.addActionListener(e -> { itemsCarrito.clear(); actualizarTablaCarrito(); });
        panelBtnItems.add(btnAgregarItem);
        panelBtnItems.add(btnVaciar);
        gbc.gridy = y++;
        gbc.insets = new Insets(6, 8, 4, 8);
        panelDerecho.add(panelBtnItems, gbc);

        lblTotalCarrito = new JLabel("Subtotal Carrito: $0 (0 ítems)");
        lblTotalCarrito.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalCarrito.setForeground(EstiloGUI.CAFE_OSCURO);
        gbc.gridy = y++;
        panelDerecho.add(lblTotalCarrito, gbc);

        y = agregarCampo(panelDerecho, gbc, y, "Descuento ($)", txtDescuento);
        y = agregarCampo(panelDerecho, gbc, y, "Impuesto ($)", txtImpuesto);

        btnConfirmarVenta = new JButton("Confirmar Venta y Descontar Kardex");
        EstiloGUI.prepararBoton(btnConfirmarVenta, true);
        btnConfirmarVenta.addActionListener(e -> confirmarVenta());
        gbc.gridy = y++;
        gbc.insets = new Insets(6, 8, 8, 8);
        panelDerecho.add(btnConfirmarVenta, gbc);

        JSeparator sep = new JSeparator();
        gbc.gridy = y++;
        gbc.insets = new Insets(2, 8, 6, 8);
        panelDerecho.add(sep, gbc);

        JLabel lblSecPago = new JLabel("Abonos / Pagos y Comprobante (RF-11, RF-18)");
        lblSecPago.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSecPago.setForeground(EstiloGUI.CAFE_OSCURO);
        gbc.gridy = y++;
        panelDerecho.add(lblSecPago, gbc);

        txtMontoPago = new JTextField();
        cmbMetodoPago = new JComboBox<>(MetodoPago.values());
        txtReferenciaPago = new JTextField();

        y = agregarCampo(panelDerecho, gbc, y, "Monto a Abonar ($) *", txtMontoPago);
        y = agregarCampo(panelDerecho, gbc, y, "Método de Pago *", cmbMetodoPago);
        y = agregarCampo(panelDerecho, gbc, y, "Referencia / Voucher", txtReferenciaPago);

        JPanel panelBtnsPago = new JPanel(new GridLayout(1, 2, 6, 0));
        panelBtnsPago.setOpaque(false);
        btnRegistrarPago = new JButton("Registrar Pago");
        btnVerComprobante = new JButton("Ver Comprobante");
        EstiloGUI.prepararBoton(btnRegistrarPago, true);
        EstiloGUI.prepararBoton(btnVerComprobante, false);
        btnRegistrarPago.addActionListener(e -> registrarPagoAbono());
        btnVerComprobante.addActionListener(e -> verComprobanteSeleccionado());
        panelBtnsPago.add(btnRegistrarPago);
        panelBtnsPago.add(btnVerComprobante);

        gbc.gridy = y++;
        gbc.insets = new Insets(8, 8, 4, 8);
        panelDerecho.add(panelBtnsPago, gbc);

        lblSeleccion = new JLabel("Seleccione una venta para registrar abonos o consultar su comprobante detallado.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(tabsIzquierda, BorderLayout.CENTER);
        panelCentro.add(panelDerecho, BorderLayout.EAST);
        panelCentro.add(lblSeleccion, BorderLayout.SOUTH);

        panelFondo.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelFondo);
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent comp) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.insets = new Insets(2, 8, 1, 8);
        gbc.gridy = y++;
        panel.add(lbl, gbc);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        panel.add(comp, gbc);
        return y;
    }
}
