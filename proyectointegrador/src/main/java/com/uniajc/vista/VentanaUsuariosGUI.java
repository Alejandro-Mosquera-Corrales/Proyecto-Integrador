// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Administracion de Usuarios y Roles con SHA-256 (RF-02, RN-09, RNF-01)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorAuth;
import com.uniajc.modelo.EstadoUsuario;
import com.uniajc.modelo.Rol;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaUsuariosGUI extends JFrame {

    private final Usuario usuarioActual;
    private final ControladorAuth ctrlAuth;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtCorreo;
    private JTextField txtTelefono;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cmbRol;
    private JComboBox<EstadoUsuario> cmbEstado;

    private JButton btnGuardar;
    private JButton btnActualizarRolEstado;
    private JButton btnLimpiar;
    private JLabel lblSeleccion;

    private int idUsuarioSeleccionado = 0;

    public VentanaUsuariosGUI(Usuario usuarioActual, ControladorAuth ctrlAuth) {
        this.usuarioActual = usuarioActual;
        this.ctrlAuth = ctrlAuth;
        initComponents();
        cargarRoles();
        cargarUsuarios();
    }

    private void cargarRoles() {
        try {
            cmbRol.removeAllItems();
            List<Rol> roles = ctrlAuth.listarRoles();
            for (Rol r : roles) {
                cmbRol.addItem(r);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> lista = ctrlAuth.listarUsuarios();
            modeloTabla.setRowCount(0);
            for (Usuario u : lista) {
                modeloTabla.addRow(new Object[]{
                    u.getIdUsuario(),
                    u.getNombreUsuario(),
                    u.getNombre(),
                    u.getApellido(),
                    u.getCorreo(),
                    u.getTelefono(),
                    u.getRol() != null ? u.getRol().getNombreRol() : "",
                    u.getEstado()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarNuevoUsuario() {
        try {
            Rol rol = (Rol) cmbRol.getSelectedItem();
            EstadoUsuario estado = (EstadoUsuario) cmbEstado.getSelectedItem();
            boolean ok = ctrlAuth.registrarUsuario(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtCorreo.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtUsername.getText().trim(),
                    new String(txtPassword.getPassword()),
                    rol,
                    estado
            );
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Usuario registrado exitosamente con contraseña cifrada en SHA-256 (RF-02, RNF-01).");
                limpiar();
                cargarUsuarios();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo crear el usuario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstadoYRol() {
        if (idUsuarioSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla para actualizar su rol o estado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Rol rol = (Rol) cmbRol.getSelectedItem();
            EstadoUsuario estado = (EstadoUsuario) cmbEstado.getSelectedItem();
            if (rol == null) return;
            boolean ok = ctrlAuth.actualizarRolYEstado(idUsuarioSeleccionado, rol.getIdRol(), estado);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Rol y estado del usuario #" + idUsuarioSeleccionado + " actualizados (RN-09).");
                cargarUsuarios();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al actualizar usuario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasarFilaAFormulario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) return;
        int mf = tablaUsuarios.convertRowIndexToModel(fila);
        idUsuarioSeleccionado = (int) modeloTabla.getValueAt(mf, 0);
        txtUsername.setText(String.valueOf(modeloTabla.getValueAt(mf, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(mf, 2)));
        txtApellido.setText(String.valueOf(modeloTabla.getValueAt(mf, 3)));
        txtCorreo.setText(modeloTabla.getValueAt(mf, 4) != null ? String.valueOf(modeloTabla.getValueAt(mf, 4)) : "");
        txtTelefono.setText(modeloTabla.getValueAt(mf, 5) != null ? String.valueOf(modeloTabla.getValueAt(mf, 5)) : "");
        Object est = modeloTabla.getValueAt(mf, 7);
        if (est instanceof EstadoUsuario) {
            cmbEstado.setSelectedItem(est);
        }
        lblSeleccion.setText("Usuario seleccionado: ID #" + idUsuarioSeleccionado + " (" + txtUsername.getText() + ")");
    }

    private void limpiar() {
        idUsuarioSeleccionado = 0;
        txtNombre.setText("");
        txtApellido.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        cmbEstado.setSelectedItem(EstadoUsuario.ACTIVO);
        tablaUsuarios.clearSelection();
        lblSeleccion.setText("RN-09: Un usuario INACTIVO no puede iniciar sesión ni operar el sistema.");
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Usuarios y Roles");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1080, 640);
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

        String rolStr = usuarioActual.getRol() != null ? usuarioActual.getRol().getNombreRol() : "Sin rol";
        JLabel lblUser = new JLabel("Usuario: " + usuarioActual.getNombreCompleto() + " | Rol: " + rolStr);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        panelHeader.add(panelMarca, BorderLayout.WEST);
        panelHeader.add(lblUser, BorderLayout.EAST);
        panelFondo.add(panelHeader, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(14, 10));
        panelCentro.setBackground(EstiloGUI.FONDO);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        JLabel lblTitulo = new JLabel("Administración de Usuarios y Perfiles de Rol (RF-02, RN-09)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);
        panelCentro.add(lblTitulo, BorderLayout.NORTH);

        String[] cols = {"ID", "Login", "Nombre", "Apellido", "Correo", "Teléfono", "Rol Asignado", "Estado"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        EstiloGUI.configurarTabla(tablaUsuarios);
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pasarFilaAFormulario();
        });

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(EstiloGUI.BLANCO);
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));
        panelForm.setPreferredSize(new Dimension(310, 480));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        txtNombre = new JTextField();
        txtApellido = new JTextField();
        txtCorreo = new JTextField();
        txtTelefono = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        cmbRol = new JComboBox<>();
        cmbEstado = new JComboBox<>(EstadoUsuario.values());

        y = agregarCampo(panelForm, gbc, y, "Nombre *", txtNombre);
        y = agregarCampo(panelForm, gbc, y, "Apellido *", txtApellido);
        y = agregarCampo(panelForm, gbc, y, "Correo Electrónico", txtCorreo);
        y = agregarCampo(panelForm, gbc, y, "Teléfono", txtTelefono);
        y = agregarCampo(panelForm, gbc, y, "Usuario (Login) *", txtUsername);
        y = agregarCampo(panelForm, gbc, y, "Contraseña (Nuevos) *", txtPassword);
        y = agregarCampo(panelForm, gbc, y, "Rol del Sistema *", cmbRol);
        y = agregarCampo(panelForm, gbc, y, "Estado (RN-09) *", cmbEstado);

        btnGuardar = new JButton("Crear Usuario");
        btnActualizarRolEstado = new JButton("Cambiar Rol/Estado");
        btnLimpiar = new JButton("Limpiar Formulario");
        EstiloGUI.prepararBoton(btnGuardar, true);
        EstiloGUI.prepararBoton(btnActualizarRolEstado, false);
        EstiloGUI.prepararBoton(btnLimpiar, false);

        btnGuardar.addActionListener(e -> guardarNuevoUsuario());
        btnActualizarRolEstado.addActionListener(e -> actualizarEstadoYRol());
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel panelBtns = new JPanel(new GridLayout(3, 1, 6, 6));
        panelBtns.setOpaque(false);
        panelBtns.add(btnGuardar);
        panelBtns.add(btnActualizarRolEstado);
        panelBtns.add(btnLimpiar);

        gbc.gridy = y++;
        gbc.insets = new Insets(10, 8, 4, 8);
        panelForm.add(panelBtns, gbc);

        lblSeleccion = new JLabel("RN-09: Un usuario INACTIVO no puede iniciar sesión ni operar el sistema.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
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
