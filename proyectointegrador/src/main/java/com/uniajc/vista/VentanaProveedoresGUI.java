// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing para gestion de Proveedores (RF-04)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorProveedor;
import com.uniajc.modelo.Proveedor;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaProveedoresGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorProveedor ctrlProveedor;

    private JTable tablaDatos;
    private DefaultTableModel modeloTabla;
    private JTextField txtNit;
    private JTextField txtRazonSocial;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnLimpiar;
    private JButton btnRefrescar;
    private JLabel lblSeleccion;

    private int idProveedorSeleccionado = 0;

    public VentanaProveedoresGUI(Usuario usuario, ControladorProveedor ctrlProveedor) {
        this.usuario = usuario;
        this.ctrlProveedor = ctrlProveedor;
        initComponents();
        aplicarPermisos();
        cargarDatos();
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        boolean puede = r.contains("ADMIN") || r.contains("INVENTARIO");
        btnGuardar.setEnabled(puede);
        btnActualizar.setEnabled(puede);
    }

    private void cargarDatos() {
        try {
            List<Proveedor> lista = ctrlProveedor.listarProveedores();
            modeloTabla.setRowCount(0);
            for (Proveedor p : lista) {
                modeloTabla.addRow(new Object[]{
                    p.getIdProveedor(),
                    p.getNitDocumento(),
                    p.getRazonSocial(),
                    p.getTelefono(),
                    p.getCorreo()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar proveedores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        try {
            boolean ok = ctrlProveedor.registrarProveedor(
                    txtNit.getText().trim(),
                    txtRazonSocial.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtCorreo.getText().trim()
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Proveedor registrado exitosamente.");
                limpiar();
                cargarDatos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar proveedor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizar() {
        if (idProveedorSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean ok = ctrlProveedor.actualizarProveedor(
                    idProveedorSeleccionado,
                    txtNit.getText().trim(),
                    txtRazonSocial.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtCorreo.getText().trim()
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Proveedor actualizado exitosamente.");
                cargarDatos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al actualizar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasarFilaAFormulario() {
        int fila = tablaDatos.getSelectedRow();
        if (fila < 0) return;
        int mf = tablaDatos.convertRowIndexToModel(fila);
        idProveedorSeleccionado = (int) modeloTabla.getValueAt(mf, 0);
        txtNit.setText(String.valueOf(modeloTabla.getValueAt(mf, 1)));
        txtRazonSocial.setText(String.valueOf(modeloTabla.getValueAt(mf, 2)));
        txtTelefono.setText(modeloTabla.getValueAt(mf, 3) != null ? String.valueOf(modeloTabla.getValueAt(mf, 3)) : "");
        txtCorreo.setText(modeloTabla.getValueAt(mf, 4) != null ? String.valueOf(modeloTabla.getValueAt(mf, 4)) : "");
        lblSeleccion.setText("Proveedor seleccionado: ID #" + idProveedorSeleccionado);
    }

    private void limpiar() {
        idProveedorSeleccionado = 0;
        txtNit.setText("");
        txtRazonSocial.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        tablaDatos.clearSelection();
        lblSeleccion.setText("Seleccione un proveedor o registre uno nuevo.");
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Proveedores");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1020, 600);
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

        JLabel lblTitulo = new JLabel("Directorio de Proveedores (RF-04)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);
        panelCentro.add(lblTitulo, BorderLayout.NORTH);

        String[] cols = {"ID", "NIT / Documento", "Razón Social", "Teléfono", "Correo"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDatos = new JTable(modeloTabla);
        EstiloGUI.configurarTabla(tablaDatos);
        tablaDatos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pasarFilaAFormulario();
        });

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(EstiloGUI.BLANCO);
        panelForm.setBorder(BorderFactory.createTitledBorder("Formulario de Proveedor"));
        panelForm.setPreferredSize(new Dimension(295, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        txtNit = new JTextField();
        txtRazonSocial = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();

        y = agregarCampo(panelForm, gbc, y, "NIT / Documento *", txtNit);
        y = agregarCampo(panelForm, gbc, y, "Razón Social *", txtRazonSocial);
        y = agregarCampo(panelForm, gbc, y, "Teléfono", txtTelefono);
        y = agregarCampo(panelForm, gbc, y, "Correo Electrónico", txtCorreo);

        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnLimpiar = new JButton("Limpiar");
        btnRefrescar = new JButton("Refrescar");
        EstiloGUI.prepararBoton(btnGuardar, true);
        EstiloGUI.prepararBoton(btnActualizar, false);
        EstiloGUI.prepararBoton(btnLimpiar, false);
        EstiloGUI.prepararBoton(btnRefrescar, false);

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> cargarDatos());

        JPanel panelBtns = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBtns.setOpaque(false);
        panelBtns.add(btnGuardar);
        panelBtns.add(btnActualizar);
        panelBtns.add(btnLimpiar);
        panelBtns.add(btnRefrescar);

        gbc.gridy = y++;
        gbc.insets = new Insets(12, 8, 4, 8);
        panelForm.add(panelBtns, gbc);

        lblSeleccion = new JLabel("Seleccione un proveedor o registre uno nuevo.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
        panelCentro.add(panelForm, BorderLayout.EAST);
        panelCentro.add(lblSeleccion, BorderLayout.SOUTH);

        panelFondo.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelFondo);
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent comp) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = y++;
        panel.add(lbl, gbc);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        panel.add(comp, gbc);
        return y;
    }
}
