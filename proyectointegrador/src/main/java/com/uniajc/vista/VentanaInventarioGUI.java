// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Kardex, Solicitudes de Movimiento y Autorizacion (RF-07 a RF-17, RN-01 a RN-05)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorInventario;
import com.uniajc.controlador.ControladorProveedor;
import com.uniajc.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentanaInventarioGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorInventario ctrlInventario;
    private final ControladorProveedor ctrlProveedor;

    private JTable tablaKardex;
    private DefaultTableModel modeloKardex;
    private JTable tablaAlertas;
    private DefaultTableModel modeloAlertas;

    private JComboBox<Producto> cmbProducto;
    private JComboBox<TipoMovimiento> cmbTipoMovimiento;
    private JComboBox<Proveedor> cmbProveedor;
    private JTextField txtCantidad;
    private JTextField txtMotivo;

    private JTextField txtObsAutorizacion;
    private JButton btnSolicitar;
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JCheckBox chkSoloPendientes;
    private JLabel lblSeleccion;

    private int idMovimientoSeleccionado = 0;

    public VentanaInventarioGUI(Usuario usuario, ControladorInventario ctrlInventario, ControladorProveedor ctrlProveedor) {
        this.usuario = usuario;
        this.ctrlInventario = ctrlInventario;
        this.ctrlProveedor = ctrlProveedor;
        initComponents();
        aplicarPermisos();
        recargarTodo();
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        boolean puedeSolicitar = r.contains("ADMIN") || r.contains("INVENTARIO");
        boolean puedeAutorizar = r.contains("ADMIN") || r.contains("AUTORIZADOR");

        btnSolicitar.setEnabled(puedeSolicitar);
        btnAprobar.setEnabled(puedeAutorizar);
        btnRechazar.setEnabled(puedeAutorizar);
    }

    private void recargarTodo() {
        cargarCombos();
        cargarMovimientos();
        cargarAlertasStockBajo();
    }

    private void cargarCombos() {
        try {
            cmbProducto.removeAllItems();
            for (Producto p : ctrlInventario.listarProductos()) {
                cmbProducto.addItem(p);
            }
            cmbProveedor.removeAllItems();
            cmbProveedor.addItem(null); // Opcional para movimientos internos
            for (Proveedor prov : ctrlProveedor.listarProveedores()) {
                cmbProveedor.addItem(prov);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar listas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarMovimientos() {
        try {
            List<MovimientoInventario> lista = chkSoloPendientes.isSelected()
                    ? ctrlInventario.listarPendientesKardex()
                    : ctrlInventario.listarHistorialKardex(0);
            modeloKardex.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (MovimientoInventario m : lista) {
                modeloKardex.addRow(new Object[]{
                    m.getIdMovimiento(),
                    m.getFechaMovimiento() != null ? m.getFechaMovimiento().format(fmt) : "",
                    m.getProductoAfectado() != null ? m.getProductoAfectado().getNombreProducto() : "",
                    m.getTipo() != null ? m.getTipo().getDescripcion() : "",
                    m.getTipo() != null && m.getTipo().esEntrada() ? "ENTRADA (+)" : "SALIDA (-)",
                    m.getCantidad(),
                    m.getStockAnterior() != null ? m.getStockAnterior() : "-",
                    m.getStockNuevo() != null ? m.getStockNuevo() : "-",
                    m.getEstadoMovimiento(),
                    m.getMotivo(),
                    m.getObservacionAutorizacion() != null ? m.getObservacionAutorizacion() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar Kardex: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarAlertasStockBajo() {
        try {
            List<Producto> alertas = ctrlInventario.listarProductosAlertaStockBajo();
            modeloAlertas.setRowCount(0);
            for (Producto p : alertas) {
                modeloAlertas.addRow(new Object[]{
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getCategoria(),
                    p.getStockActual(),
                    p.getStockMinimo(),
                    p.getEstado()
                });
            }
        } catch (Exception ignored) {
        }
    }

    private void registrarSolicitud() {
        try {
            Producto prod = (Producto) cmbProducto.getSelectedItem();
            TipoMovimiento tipo = (TipoMovimiento) cmbTipoMovimiento.getSelectedItem();
            Proveedor prov = (Proveedor) cmbProveedor.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            String motivo = txtMotivo.getText().trim();

            boolean ok = ctrlInventario.solicitarMovimientoKardex(prod, tipo, cantidad, motivo, prov, usuario);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Solicitud de movimiento registrada en estado PENDIENTE (RN-03).\n"
                        + "El stock se actualizará cuando el Autorizador de Kardex o Administrador la apruebe (RF-16).");
                txtCantidad.setText("");
                txtMotivo.setText("");
                recargarTodo();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero positivo.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo solicitar el movimiento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decidirSeleccionado(boolean aprobar) {
        if (idMovimientoSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un movimiento PENDIENTE en la tabla del Kardex.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String obs = txtObsAutorizacion.getText().trim();
            boolean ok = ctrlInventario.decidirMovimientoKardex(idMovimientoSeleccionado, usuario, aprobar, obs);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        aprobar
                        ? "Movimiento #" + idMovimientoSeleccionado + " AUTORIZADO. Stock actualizado atómicamente en PostgreSQL."
                        : "Movimiento #" + idMovimientoSeleccionado + " RECHAZADO. El stock no sufrió cambios.");
                txtObsAutorizacion.setText("");
                idMovimientoSeleccionado = 0;
                recargarTodo();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al decidir movimiento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Gestión de Inventario y Kardex");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1180, 700);
        setLocationRelativeTo(null);

        JPanel panelFondo = new JPanel(new BorderLayout(0, 10));
        panelFondo.setBackground(EstiloGUI.FONDO);

        // Header
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

        // Centro
        JPanel panelCentro = new JPanel(new BorderLayout(14, 10));
        panelCentro.setBackground(EstiloGUI.FONDO);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);
        JLabel lblTitulo = new JLabel("Kardex Transaccional y Control de Existencias (RF-07 a RF-17)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);

        chkSoloPendientes = new JCheckBox("Mostrar solo movimientos PENDIENTES por autorizar (RF-16)");
        chkSoloPendientes.setOpaque(false);
        chkSoloPendientes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkSoloPendientes.setForeground(EstiloGUI.CAFE_OSCURO);
        chkSoloPendientes.addActionListener(e -> cargarMovimientos());

        panelTop.add(lblTitulo, BorderLayout.WEST);
        panelTop.add(chkSoloPendientes, BorderLayout.EAST);
        panelCentro.add(panelTop, BorderLayout.NORTH);

        // Pestañas a la izquierda: Historial Kardex y Alertas de Stock Bajo
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        String[] colsKardex = {"ID Mov", "Fecha", "Producto", "Evento Kardex", "Flujo", "Cantidad", "Stock Ant.", "Stock Nuevo", "Estado", "Motivo / Soporte", "Obs. Autorización"};
        modeloKardex = new DefaultTableModel(colsKardex, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaKardex = new JTable(modeloKardex);
        EstiloGUI.configurarTabla(tablaKardex);
        tablaKardex.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaKardex.getSelectedRow() >= 0) {
                int mf = tablaKardex.convertRowIndexToModel(tablaKardex.getSelectedRow());
                idMovimientoSeleccionado = (int) modeloKardex.getValueAt(mf, 0);
                Object est = modeloKardex.getValueAt(mf, 8);
                lblSeleccion.setText("Movimiento seleccionado: #" + idMovimientoSeleccionado + " | Estado: " + est);
            }
        });
        tabs.addTab("Movimientos de Kardex (RF-17)", new JScrollPane(tablaKardex));

        String[] colsAlertas = {"ID Prod", "Producto", "Categoría", "Stock Actual", "Stock Mínimo", "Estado"};
        modeloAlertas = new DefaultTableModel(colsAlertas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaAlertas = new JTable(modeloAlertas);
        EstiloGUI.configurarTabla(tablaAlertas);
        tabs.addTab("Alertas de Stock Bajo (RF-06)", new JScrollPane(tablaAlertas));

        // Panel Derecho con Solicitud + Autorización
        JPanel panelDerecho = new JPanel();
        panelDerecho.setBackground(EstiloGUI.BLANCO);
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Operaciones de Kardex"));
        panelDerecho.setPreferredSize(new Dimension(340, 520));
        panelDerecho.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        cmbProducto = new JComboBox<>();
        cmbTipoMovimiento = new JComboBox<>(new TipoMovimiento[]{
            TipoMovimiento.COMPRA_RECIBIDA,
            TipoMovimiento.DEVOLUCION_CLIENTE,
            TipoMovimiento.AJUSTE_POSITIVO_FISICO,
            TipoMovimiento.MERCANCIA_DANADA,
            TipoMovimiento.AJUSTE_NEGATIVO_FISICO,
            TipoMovimiento.DEVOLUCION_PROVEEDOR
        });
        cmbProveedor = new JComboBox<>();
        txtCantidad = new JTextField();
        txtMotivo = new JTextField();

        y = agregarCampo(panelDerecho, gbc, y, "1. Producto Afectado *", cmbProducto);
        y = agregarCampo(panelDerecho, gbc, y, "2. Tipo de Movimiento *", cmbTipoMovimiento);
        y = agregarCampo(panelDerecho, gbc, y, "3. Proveedor (Compras/Dev. Prov)", cmbProveedor);
        y = agregarCampo(panelDerecho, gbc, y, "4. Cantidad (Unidades > 0) *", txtCantidad);
        y = agregarCampo(panelDerecho, gbc, y, "5. Motivo / Factura / Acta *", txtMotivo);

        btnSolicitar = new JButton("Solicitar Movimiento (PENDIENTE)");
        EstiloGUI.prepararBoton(btnSolicitar, true);
        btnSolicitar.addActionListener(e -> registrarSolicitud());
        gbc.gridy = y++;
        gbc.insets = new Insets(10, 8, 12, 8);
        panelDerecho.add(btnSolicitar, gbc);

        JSeparator sep = new JSeparator();
        gbc.gridy = y++;
        gbc.insets = new Insets(4, 8, 8, 8);
        panelDerecho.add(sep, gbc);

        JLabel lblAut = new JLabel("Autorización de Kardex (RF-16 / RN-04)");
        lblAut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAut.setForeground(EstiloGUI.CAFE_OSCURO);
        gbc.gridy = y++;
        panelDerecho.add(lblAut, gbc);

        txtObsAutorizacion = new JTextField();
        y = agregarCampo(panelDerecho, gbc, y, "Observación / Motivo de Rechazo:", txtObsAutorizacion);

        JPanel panelBtnsAut = new JPanel(new GridLayout(1, 2, 8, 0));
        panelBtnsAut.setOpaque(false);
        btnAprobar = new JButton("Aprobar");
        btnRechazar = new JButton("Rechazar");
        EstiloGUI.prepararBoton(btnAprobar, true);
        EstiloGUI.prepararBoton(btnRechazar, false);
        btnAprobar.addActionListener(e -> decidirSeleccionado(true));
        btnRechazar.addActionListener(e -> decidirSeleccionado(false));
        panelBtnsAut.add(btnAprobar);
        panelBtnsAut.add(btnRechazar);

        gbc.gridy = y++;
        panelDerecho.add(panelBtnsAut, gbc);

        JButton btnRefrescar = new JButton("Refrescar Kardex y Alertas");
        EstiloGUI.prepararBoton(btnRefrescar, false);
        btnRefrescar.addActionListener(e -> recargarTodo());
        gbc.gridy = y++;
        gbc.insets = new Insets(10, 8, 4, 8);
        panelDerecho.add(btnRefrescar, gbc);

        lblSeleccion = new JLabel("Seleccione una fila del Kardex para autorizar o rechazar solicitudes pendientes.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(tabs, BorderLayout.CENTER);
        panelCentro.add(panelDerecho, BorderLayout.EAST);
        panelCentro.add(lblSeleccion, BorderLayout.SOUTH);

        panelFondo.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelFondo);
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent comp) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.insets = new Insets(3, 8, 2, 8);
        gbc.gridy = y++;
        panel.add(lbl, gbc);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        panel.add(comp, gbc);
        return y;
    }
}
