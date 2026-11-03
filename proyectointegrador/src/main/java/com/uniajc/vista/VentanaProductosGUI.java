// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Catalogo de Productos sin edicion directa de stock (RF-05, RF-06, RN-01)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorInventario;
import com.uniajc.modelo.EstadoProducto;
import com.uniajc.modelo.Producto;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class VentanaProductosGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorInventario ctrlInventario;

    private JTable tablaDatos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtCategoria;
    private JTextField txtMaterial;
    private JTextField txtColor;
    private JTextField txtPrecio;
    private JTextField txtStockMinimo;
    private JComboBox<EstadoProducto> cmbEstado;
    private JLabel lblSeleccion;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnLimpiar;
    private JButton btnRefrescar;

    private int idProductoSeleccionado = 0;

    public VentanaProductosGUI(Usuario usuario, ControladorInventario ctrlInventario) {
        this.usuario = usuario;
        this.ctrlInventario = ctrlInventario;
        initComponents();
        aplicarPermisos();
        cargarProductos(null);
    }

    private void aplicarPermisos() {
        String r = usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                ? usuario.getRol().getNombreRol().toUpperCase() : "";
        boolean puedeEditar = r.contains("ADMIN") || r.contains("INVENTARIO");
        btnGuardar.setEnabled(puedeEditar);
        btnActualizar.setEnabled(puedeEditar);
    }

    private void cargarProductos(String filtro) {
        try {
            List<Producto> lista = (filtro == null || filtro.trim().isEmpty())
                    ? ctrlInventario.listarProductos()
                    : ctrlInventario.buscarProductos(filtro.trim());
            modeloTabla.setRowCount(0);
            for (Producto p : lista) {
                modeloTabla.addRow(new Object[]{
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getCategoria(),
                    p.getMaterial(),
                    p.getColor(),
                    p.getPrecioUnitario(),
                    p.getStockActual(),
                    p.getStockReservado(),
                    p.getStockDisponible(),
                    p.getStockMinimo(),
                    p.getEstado(),
                    p.getDescripcion()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasarFilaAFormulario() {
        int fila = tablaDatos.getSelectedRow();
        if (fila < 0) return;
        int mFila = tablaDatos.convertRowIndexToModel(fila);
        idProductoSeleccionado = (int) modeloTabla.getValueAt(mFila, 0);
        txtNombre.setText(valorTexto(mFila, 1));
        txtCategoria.setText(valorTexto(mFila, 2));
        txtMaterial.setText(valorTexto(mFila, 3));
        txtColor.setText(valorTexto(mFila, 4));
        txtPrecio.setText(valorTexto(mFila, 5));
        txtStockMinimo.setText(valorTexto(mFila, 9));
        Object est = modeloTabla.getValueAt(mFila, 10);
        if (est instanceof EstadoProducto) {
            cmbEstado.setSelectedItem(est);
        }
        txtDescripcion.setText(valorTexto(mFila, 11));
        lblSeleccion.setText("Producto seleccionado: ID #" + idProductoSeleccionado
                + " | Stock actual: " + valorTexto(mFila, 6) + " (Solo modificable por Kardex - RN-01)");
    }

    private String valorTexto(int fila, int col) {
        Object v = modeloTabla.getValueAt(fila, col);
        return v == null ? "" : String.valueOf(v);
    }

    private void guardarNuevo() {
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int stockMin = Integer.parseInt(txtStockMinimo.getText().trim());
            boolean ok = ctrlInventario.crearProductoCatalogo(
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    txtCategoria.getText().trim(),
                    txtMaterial.getText().trim(),
                    txtColor.getText().trim(),
                    precio,
                    stockMin,
                    usuario
            );
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Producto registrado en el catálogo con Stock Inicial = 0 (RN-01).\n"
                        + "Para ingresar existencias físicas, registre una solicitud en Inventario / Kardex.");
                limpiar();
                cargarProductos(null);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Precio unitario y Stock mínimo deben ser valores numéricos válidos.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar producto", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarExistente() {
        if (idProductoSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para actualizar su ficha.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int stockMin = Integer.parseInt(txtStockMinimo.getText().trim());
            EstadoProducto estado = (EstadoProducto) cmbEstado.getSelectedItem();
            boolean ok = ctrlInventario.actualizarProductoCatalogo(
                    idProductoSeleccionado,
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    txtCategoria.getText().trim(),
                    txtMaterial.getText().trim(),
                    txtColor.getText().trim(),
                    precio,
                    stockMin,
                    estado,
                    usuario
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Ficha técnica del producto actualizada correctamente.");
                cargarProductos(null);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Precio unitario y Stock mínimo deben ser numéricos.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al actualizar producto", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        idProductoSeleccionado = 0;
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtCategoria.setText("Sala");
        txtMaterial.setText("Madera Roble");
        txtColor.setText("Nogal");
        txtPrecio.setText("");
        txtStockMinimo.setText("5");
        cmbEstado.setSelectedItem(EstadoProducto.DISPONIBLE);
        tablaDatos.clearSelection();
        lblSeleccion.setText("Regla de Oro (RN-01): Todo producto nuevo inicia con Stock = 0. El stock solo cambia por Kardex.");
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Catálogo de Productos");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1120, 680);
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

        // Centro: Tabla + Formulario
        JPanel panelCentro = new JPanel(new BorderLayout(14, 10));
        panelCentro.setBackground(EstiloGUI.FONDO);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        // Barra superior de módulo y búsqueda
        JPanel panelTopModulo = new JPanel(new BorderLayout(10, 0));
        panelTopModulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Catálogo de Productos (RF-05, RF-06)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBusqueda.setOpaque(false);
        txtBuscar = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnVerTodos = new JButton("Ver Todos");
        EstiloGUI.prepararBoton(btnBuscar, true);
        EstiloGUI.prepararBoton(btnVerTodos, false);
        btnBuscar.addActionListener(e -> cargarProductos(txtBuscar.getText()));
        btnVerTodos.addActionListener(e -> { txtBuscar.setText(""); cargarProductos(null); });
        panelBusqueda.add(new JLabel("Filtrar nombre/descripción:"));
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnVerTodos);

        panelTopModulo.add(lblTitulo, BorderLayout.WEST);
        panelTopModulo.add(panelBusqueda, BorderLayout.EAST);
        panelCentro.add(panelTopModulo, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Nombre", "Categoría", "Material", "Color", "Precio Unit.", "Stock Actual", "Reservado", "Disponible", "Stock Mín.", "Estado", "Descripción"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDatos = new JTable(modeloTabla);
        EstiloGUI.configurarTabla(tablaDatos);
        tablaDatos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pasarFilaAFormulario();
        });
        JScrollPane scrollTabla = new JScrollPane(tablaDatos);

        // Panel Formulario Derecho
        JPanel panelForm = new JPanel();
        panelForm.setBackground(EstiloGUI.BLANCO);
        panelForm.setBorder(BorderFactory.createTitledBorder("Ficha de Catálogo (Stock inicial = 0)"));
        panelForm.setPreferredSize(new Dimension(310, 480));
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int y = 0;

        txtNombre = new JTextField();
        txtDescripcion = new JTextField();
        txtCategoria = new JTextField("Sala");
        txtMaterial = new JTextField("Madera Roble");
        txtColor = new JTextField("Nogal");
        txtPrecio = new JTextField();
        txtStockMinimo = new JTextField("5");
        cmbEstado = new JComboBox<>(EstadoProducto.values());

        y = agregarCampo(panelForm, gbc, y, "Nombre del Producto *", txtNombre);
        y = agregarCampo(panelForm, gbc, y, "Descripción", txtDescripcion);
        y = agregarCampo(panelForm, gbc, y, "Categoría *", txtCategoria);
        y = agregarCampo(panelForm, gbc, y, "Material", txtMaterial);
        y = agregarCampo(panelForm, gbc, y, "Color", txtColor);
        y = agregarCampo(panelForm, gbc, y, "Precio Unitario ($) *", txtPrecio);
        y = agregarCampo(panelForm, gbc, y, "Stock Mínimo (Alerta RF-06) *", txtStockMinimo);
        y = agregarCampo(panelForm, gbc, y, "Estado Catálogo", cmbEstado);

        JPanel panelBtns = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBtns.setOpaque(false);
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar Ficha");
        btnLimpiar = new JButton("Limpiar");
        btnRefrescar = new JButton("Refrescar");

        EstiloGUI.prepararBoton(btnGuardar, true);
        EstiloGUI.prepararBoton(btnActualizar, false);
        EstiloGUI.prepararBoton(btnLimpiar, false);
        EstiloGUI.prepararBoton(btnRefrescar, false);

        btnGuardar.addActionListener(e -> guardarNuevo());
        btnActualizar.addActionListener(e -> actualizarExistente());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> cargarProductos(null));

        panelBtns.add(btnGuardar);
        panelBtns.add(btnActualizar);
        panelBtns.add(btnLimpiar);
        panelBtns.add(btnRefrescar);

        gbc.gridy = y++;
        gbc.insets = new Insets(12, 8, 4, 8);
        panelForm.add(panelBtns, gbc);

        lblSeleccion = new JLabel("Regla de Oro (RN-01): Todo producto nuevo inicia con Stock = 0. El stock solo cambia por Kardex.");
        lblSeleccion.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelCentro.add(scrollTabla, BorderLayout.CENTER);
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
