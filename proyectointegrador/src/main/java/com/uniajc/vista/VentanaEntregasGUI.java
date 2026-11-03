// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Entregas a Domicilio con avance secuencial de estado (RF-19 a RF-21, RN-08)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorAuth;
import com.uniajc.controlador.ControladorEntrega;
import com.uniajc.modelo.Entrega;
import com.uniajc.modelo.EstadoEntrega;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentanaEntregasGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorEntrega ctrlEntrega;
    private final ControladorAuth ctrlAuth;

    private JTable tablaEntregas;
    private DefaultTableModel modeloTabla;

    private JTextField txtIdVenta;
    private JTextField txtDireccion;
    private JTextField txtNombreRecibe;
    private JTextField txtDocRecibe;
    private JTextField txtTelRecibe;
    private JComboBox<Usuario> cmbTransportador;
    private JTextField txtObservacion;
    private JComboBox<EstadoEntrega> cmbNuevoEstado;

    private JButton btnProgramar;
    private JButton btnAvanzarEstado;
    private JButton btnRefrescar;
    private JLabel lblSeleccion;

    private int idEntregaSeleccionada = 0;

    public VentanaEntregasGUI(Usuario usuario, ControladorEntrega ctrlEntrega, ControladorAuth ctrlAuth) {
        this.usuario = usuario;
        this.ctrlEntrega = ctrlEntrega;
        this.ctrlAuth = ctrlAuth;
        initComponents();
        aplicarPermisos();
        cargarTransportadores();
        cargarEntregas();
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        boolean puedeProgramar = r.contains("ADMIN") || r.contains("VENDEDOR");
        boolean puedeAvanzar = r.contains("ADMIN") || r.contains("TRANSPORTADOR");
        btnProgramar.setEnabled(puedeProgramar);
        btnAvanzarEstado.setEnabled(puedeAvanzar);
    }

    private void cargarTransportadores() {
        try {
            cmbTransportador.removeAllItems();
            List<Usuario> usuarios = ctrlAuth.listarUsuarios();
            for (Usuario u : usuarios) {
                if (u.estaActivo()) {
                    cmbTransportador.addItem(u);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void cargarEntregas() {
        try {
            List<Entrega> lista = ctrlEntrega.listarEntregasPorUsuario(usuario);
            modeloTabla.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Entrega e : lista) {
                modeloTabla.addRow(new Object[]{
                    e.getIdEntrega(),
                    e.getIdVenta(),
                    e.getDireccionEntrega(),
                    e.getNombreRecibe(),
                    e.getDocumentoRecibe(),
                    e.getTelefonoRecibe(),
                    e.getTransportador() != null ? e.getTransportador().getNombreCompleto() : "",
                    e.getEstado(),
                    e.getFechaProgramada() != null ? e.getFechaProgramada().format(fmt) : "",
                    e.getFechaSalidaBodega() != null ? e.getFechaSalidaBodega().format(fmt) : "-",
                    e.getFechaEntrega() != null ? e.getFechaEntrega().format(fmt) : "-",
                    e.getObservacion() != null ? e.getObservacion() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar entregas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void programarNueva() {
        try {
            int idVenta = Integer.parseInt(txtIdVenta.getText().trim());
            Usuario transp = (Usuario) cmbTransportador.getSelectedItem();
            boolean ok = ctrlEntrega.programarEntrega(
                    idVenta,
                    txtDireccion.getText().trim(),
                    txtNombreRecibe.getText().trim(),
                    txtDocRecibe.getText().trim(),
                    txtTelRecibe.getText().trim(),
                    LocalDateTime.now().plusDays(1),
                    txtObservacion.getText().trim(),
                    transp
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Entrega programada en estado 'En bodega' (RF-19).");
                limpiar();
                cargarEntregas();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "El ID de la venta debe ser numérico.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo programar la entrega", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void avanzarEstado() {
        if (idEntregaSeleccionada <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una entrega de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            EstadoEntrega nuevoEst = (EstadoEntrega) cmbNuevoEstado.getSelectedItem();
            boolean ok = ctrlEntrega.avanzarEstadoEntrega(
                    idEntregaSeleccionada,
                    usuario,
                    nuevoEst,
                    txtObservacion.getText().trim()
            );
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Estado de entrega #" + idEntregaSeleccionada + " actualizado a '" + nuevoEst + "' (RF-21).\n"
                        + "El inventario NO se vuelve a descontar (RN-08).");
                cargarEntregas();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo actualizar el estado. Verifique que usted sea el transportador asignado.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transición inválida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        idEntregaSeleccionada = 0;
        txtIdVenta.setText("");
        txtDireccion.setText("");
        txtNombreRecibe.setText("");
        txtDocRecibe.setText("");
        txtTelRecibe.setText("");
        txtObservacion.setText("");
        tablaEntregas.clearSelection();
        lblSeleccion.setText("Flujo obligatorio (RN-08): En bodega -> En ruta -> Entregado.");
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Entregas y Despachos a Domicilio");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1160, 660);
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

        JLabel lblTitulo = new JLabel("Control Logístico de Entregas a Domicilio (RF-19, RF-20, RF-21)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);
        panelCentro.add(lblTitulo, BorderLayout.NORTH);

        String[] cols = {"ID Entrega", "ID Venta", "Dirección", "Recibe", "Doc. Recibe", "Teléfono", "Transportador", "Estado", "Programada", "Salida Bodega", "Entregado", "Observación"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaEntregas = new JTable(modeloTabla);
        EstiloGUI.configurarTabla(tablaEntregas);
        tablaEntregas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaEntregas.getSelectedRow() >= 0) {
                int mf = tablaEntregas.convertRowIndexToModel(tablaEntregas.getSelectedRow());
                idEntregaSeleccionada = (int) modeloTabla.getValueAt(mf, 0);
                Object est = modeloTabla.getValueAt(mf, 7);
                lblSeleccion.setText("Entrega seleccionada: #" + idEntregaSeleccionada + " | Estado actual: " + est);
                if (est == EstadoEntrega.EN_BODEGA) {
                    cmbNuevoEstado.setSelectedItem(EstadoEntrega.EN_RUTA);
                } else if (est == EstadoEntrega.EN_RUTA) {
                    cmbNuevoEstado.setSelectedItem(EstadoEntrega.ENTREGADO);
                }
            }
        });

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(EstiloGUI.BLANCO);
        panelForm.setBorder(BorderFactory.createTitledBorder("Programación y Avance de Estado"));
        panelForm.setPreferredSize(new Dimension(320, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        txtIdVenta = new JTextField();
        txtDireccion = new JTextField();
        txtNombreRecibe = new JTextField();
        txtDocRecibe = new JTextField();
        txtTelRecibe = new JTextField();
        cmbTransportador = new JComboBox<>();
        txtObservacion = new JTextField();
        cmbNuevoEstado = new JComboBox<>(new EstadoEntrega[]{EstadoEntrega.EN_RUTA, EstadoEntrega.ENTREGADO});

        y = agregarCampo(panelForm, gbc, y, "ID de Venta Asociada *", txtIdVenta);
        y = agregarCampo(panelForm, gbc, y, "Dirección de Entrega *", txtDireccion);
        y = agregarCampo(panelForm, gbc, y, "Nombre de Quien Recibe *", txtNombreRecibe);
        y = agregarCampo(panelForm, gbc, y, "Documento de Quien Recibe", txtDocRecibe);
        y = agregarCampo(panelForm, gbc, y, "Teléfono de Contacto", txtTelRecibe);
        y = agregarCampo(panelForm, gbc, y, "Transportador Asignado *", cmbTransportador);
        y = agregarCampo(panelForm, gbc, y, "Observación Logística", txtObservacion);

        btnProgramar = new JButton("Programar Entrega (En Bodega)");
        EstiloGUI.prepararBoton(btnProgramar, true);
        btnProgramar.addActionListener(e -> programarNueva());
        gbc.gridy = y++;
        gbc.insets = new Insets(8, 8, 8, 8);
        panelForm.add(btnProgramar, gbc);

        JSeparator sep = new JSeparator();
        gbc.gridy = y++;
        gbc.insets = new Insets(4, 8, 6, 8);
        panelForm.add(sep, gbc);

        y = agregarCampo(panelForm, gbc, y, "Avanzar Estado (RF-21 / RN-08):", cmbNuevoEstado);

        btnAvanzarEstado = new JButton("Confirmar Nuevo Estado");
        btnRefrescar = new JButton("Refrescar Entregas");
        EstiloGUI.prepararBoton(btnAvanzarEstado, true);
        EstiloGUI.prepararBoton(btnRefrescar, false);
        btnAvanzarEstado.addActionListener(e -> avanzarEstado());
        btnRefrescar.addActionListener(e -> cargarEntregas());

        JPanel panelBtns = new JPanel(new GridLayout(2, 1, 6, 6));
        panelBtns.setOpaque(false);
        panelBtns.add(btnAvanzarEstado);
        panelBtns.add(btnRefrescar);
        gbc.gridy = y++;
        gbc.insets = new Insets(8, 8, 4, 8);
        panelForm.add(panelBtns, gbc);

        lblSeleccion = new JLabel("Flujo obligatorio (RN-08): En bodega -> En ruta -> Entregado.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(new JScrollPane(tablaEntregas), BorderLayout.CENTER);
        panelCentro.add(panelForm, BorderLayout.EAST);
        panelCentro.add(lblSeleccion, BorderLayout.SOUTH);

        panelFondo.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelFondo);
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent comp) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.insets = new Insets(2, 8, 1, 8);
        gbc.gridy = y++;
        panel.add(lbl, gbc);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        panel.add(comp, gbc);
        return y;
    }
}
