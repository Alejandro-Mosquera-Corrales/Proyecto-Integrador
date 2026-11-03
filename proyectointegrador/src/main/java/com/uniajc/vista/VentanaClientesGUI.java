// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing para gestion de Clientes con validacion de documento unico (RF-03)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorCliente;
import com.uniajc.modelo.Cliente;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaClientesGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorCliente ctrlCliente;

    private JTable tablaDatos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscarDoc;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDocumento;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnRefrescar;
    private JLabel lblSeleccion;

    public VentanaClientesGUI(Usuario usuario, ControladorCliente ctrlCliente) {
        this.usuario = usuario;
        this.ctrlCliente = ctrlCliente;
        initComponents();
        aplicarPermisos();
        cargarDatos();
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        btnGuardar.setEnabled(r.contains("ADMIN") || r.contains("VENDEDOR"));
    }

    private void cargarDatos() {
        try {
            List<Cliente> clientes = ctrlCliente.listarClientes();
            modeloTabla.setRowCount(0);
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
                    c.getIdCliente(),
                    c.getDocumento(),
                    c.getNombre(),
                    c.getApellido(),
                    c.getTelefono(),
                    c.getCorreo(),
                    c.getDireccion()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al listar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPorDocumento() {
        String doc = txtBuscarDoc.getText().trim();
        if (doc.isEmpty()) {
            cargarDatos();
            return;
        }
        try {
            Cliente c = ctrlCliente.buscarPorDocumento(doc);
            modeloTabla.setRowCount(0);
            if (c != null) {
                modeloTabla.addRow(new Object[]{
                    c.getIdCliente(), c.getDocumento(), c.getNombre(),
                    c.getApellido(), c.getTelefono(), c.getCorreo(), c.getDireccion()
                });
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró cliente con documento: " + doc);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Búsqueda", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardar() {
        try {
            boolean ok = ctrlCliente.registrarCliente(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtDocumento.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtCorreo.getText().trim(),
                    txtDireccion.getText().trim()
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cliente registrado exitosamente en PostgreSQL.");
                limpiar();
                cargarDatos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar el cliente", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasarFilaAFormulario() {
        int fila = tablaDatos.getSelectedRow();
        if (fila < 0) return;
        int mf = tablaDatos.convertRowIndexToModel(fila);
        lblSeleccion.setText("Cliente seleccionado: ID #" + modeloTabla.getValueAt(mf, 0));
        txtDocumento.setText(String.valueOf(modeloTabla.getValueAt(mf, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(mf, 2)));
        txtApellido.setText(String.valueOf(modeloTabla.getValueAt(mf, 3)));
        txtTelefono.setText(modeloTabla.getValueAt(mf, 4) != null ? String.valueOf(modeloTabla.getValueAt(mf, 4)) : "");
        txtCorreo.setText(modeloTabla.getValueAt(mf, 5) != null ? String.valueOf(modeloTabla.getValueAt(mf, 5)) : "");
        txtDireccion.setText(modeloTabla.getValueAt(mf, 6) != null ? String.valueOf(modeloTabla.getValueAt(mf, 6)) : "");
    }

    private void limpiar() {
        txtNombre.setText("");
        txtApellido.setText("");
        txtDocumento.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        tablaDatos.clearSelection();
        lblSeleccion.setText("Complete el formulario para registrar un nuevo cliente.");
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Clientes");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1060, 640);
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

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);
        JLabel lblTitulo = new JLabel("Gestión de Clientes (RF-03)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);

        JPanel panelBusq = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBusq.setOpaque(false);
        txtBuscarDoc = new JTextField(16);
        JButton btnBuscar = new JButton("Buscar Documento");
        JButton btnTodos = new JButton("Ver Todos");
        EstiloGUI.prepararBoton(btnBuscar, true);
        EstiloGUI.prepararBoton(btnTodos, false);
        btnBuscar.addActionListener(e -> buscarPorDocumento());
        btnTodos.addActionListener(e -> { txtBuscarDoc.setText(""); cargarDatos(); });
        panelBusq.add(new JLabel("Documento:"));
        panelBusq.add(txtBuscarDoc);
        panelBusq.add(btnBuscar);
        panelBusq.add(btnTodos);

        panelTop.add(lblTitulo, BorderLayout.WEST);
        panelTop.add(panelBusq, BorderLayout.EAST);
        panelCentro.add(panelTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Documento", "Nombre", "Apellido", "Teléfono", "Correo", "Dirección"};
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
        panelForm.setBorder(BorderFactory.createTitledBorder("Formulario de Cliente"));
        panelForm.setPreferredSize(new Dimension(295, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        txtDocumento = new JTextField();
        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();
        txtDireccion = new JTextField();

        y = agregarCampo(panelForm, gbc, y, "Documento / Cédula *", txtDocumento);
        y = agregarCampo(panelForm, gbc, y, "Nombre *", txtNombre);
        y = agregarCampo(panelForm, gbc, y, "Apellido *", txtApellido);
        y = agregarCampo(panelForm, gbc, y, "Teléfono", txtTelefono);
        y = agregarCampo(panelForm, gbc, y, "Correo Electrónico", txtCorreo);
        y = agregarCampo(panelForm, gbc, y, "Dirección Residencia", txtDireccion);

        btnGuardar = new JButton("Guardar Cliente");
        btnLimpiar = new JButton("Limpiar");
        btnRefrescar = new JButton("Refrescar");
        EstiloGUI.prepararBoton(btnGuardar, true);
        EstiloGUI.prepararBoton(btnLimpiar, false);
        EstiloGUI.prepararBoton(btnRefrescar, false);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> cargarDatos());

        JPanel panelBtns = new JPanel(new GridLayout(3, 1, 6, 6));
        panelBtns.setOpaque(false);
        panelBtns.add(btnGuardar);
        panelBtns.add(btnLimpiar);
        panelBtns.add(btnRefrescar);

        gbc.gridy = y++;
        gbc.insets = new Insets(12, 8, 4, 8);
        panelForm.add(panelBtns, gbc);

        lblSeleccion = new JLabel("Complete el formulario para registrar un nuevo cliente.");
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
