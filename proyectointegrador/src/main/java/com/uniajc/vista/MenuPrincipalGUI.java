// Autor: Jahel Gutiérrez | Commit 6: Menu Principal Swing con control de acceso basado en los 6 roles (RF-01, RF-02)
package com.uniajc.vista;

import com.uniajc.controlador.*;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipalGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorAuth ctrlAuth;
    private final ControladorInventario ctrlInventario;
    private final ControladorCliente ctrlCliente;
    private final ControladorProveedor ctrlProveedor;
    private final ControladorVenta ctrlVenta;
    private final ControladorEntrega ctrlEntrega;

    private JPanel panelFondo;
    private JPanel panelHeader;
    private JLabel lblLogo;
    private JLabel lblMarca;
    private JLabel lblUsuario;
    private JLabel lblBienvenida;
    private JLabel lblRol;
    private JPanel panelBotones;

    private JButton btnProductos;
    private JButton btnInventario;
    private JButton btnClientes;
    private JButton btnProveedores;
    private JButton btnVentas;
    private JButton btnEntregas;
    private JButton btnUsuarios;
    private JButton btnReportes;
    private JButton btnAyuda;
    private JButton btnCerrarSesion;

    public MenuPrincipalGUI(Usuario usuario) {
        this(usuario, new ControladorAuth());
    }

    public MenuPrincipalGUI(Usuario usuario, ControladorAuth ctrlAuth) {
        this.usuario = usuario;
        this.ctrlAuth = ctrlAuth;
        this.ctrlInventario = new ControladorInventario();
        this.ctrlCliente = new ControladorCliente();
        this.ctrlProveedor = new ControladorProveedor();
        this.ctrlVenta = new ControladorVenta();
        this.ctrlEntrega = new ControladorEntrega();
        initComponents();
        configurarSegunUsuario();
    }

    private void configurarSegunUsuario() {
        setTitle("Mobilia Haus - Menú Principal");
        setLocationRelativeTo(null);
        EstiloGUI.ponerLogo(lblLogo, 60, 60);

        String nombreRol = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "Sin rol";
        lblUsuario.setText("Usuario: " + usuario.getNombreCompleto() + " | Rol: " + nombreRol);
        lblBienvenida.setText("Bienvenido(a), " + usuario.getNombre());
        lblRol.setText(descripcionPermisos(nombreRol));

        JButton[] botones = {
            btnProductos, btnInventario, btnClientes, btnProveedores,
            btnVentas, btnEntregas, btnUsuarios, btnReportes,
            btnAyuda, btnCerrarSesion
        };
        for (JButton b : botones) {
            EstiloGUI.prepararBoton(b, true);
        }
        EstiloGUI.prepararBoton(btnCerrarSesion, false);

        String r = nombreRol.toUpperCase();
        boolean esAdmin = r.contains("ADMIN");
        boolean esGerente = r.contains("GERENTE") || r.contains("AUDITOR");
        boolean esVendedor = r.contains("VENDEDOR");
        boolean esInventario = r.contains("INVENTARIO");
        boolean esAutorizador = r.contains("AUTORIZADOR");
        boolean esTransportador = r.contains("TRANSPORTADOR");

        btnProductos.setEnabled(esAdmin || esGerente || esVendedor || esInventario || esAutorizador);
        btnInventario.setEnabled(esAdmin || esGerente || esInventario || esAutorizador);
        btnClientes.setEnabled(esAdmin || esGerente || esVendedor);
        btnProveedores.setEnabled(esAdmin || esGerente || esInventario);
        btnVentas.setEnabled(esAdmin || esGerente || esVendedor);
        btnEntregas.setEnabled(esAdmin || esGerente || esVendedor || esTransportador);
        btnUsuarios.setEnabled(esAdmin);
        btnReportes.setEnabled(esAdmin || esGerente);
    }

    private String descripcionPermisos(String nombreRol) {
        String r = nombreRol != null ? nombreRol.toUpperCase() : "";
        if (r.contains("ADMIN")) {
            return "Acceso completo a todos los módulos, Kardex, Usuarios y Reportes.";
        }
        if (r.contains("GERENTE") || r.contains("AUDITOR")) {
            return "Perfil de consulta ejecutiva y auditoría de Kardex, Ventas y Reportes.";
        }
        if (r.contains("VENDEDOR")) {
            return "Acceso a Catálogo de Productos, Clientes, Ventas/Pagos y programación de Entregas.";
        }
        if (r.contains("INVENTARIO")) {
            return "Acceso a Catálogo de Productos, Proveedores y solicitud de movimientos en Kardex.";
        }
        if (r.contains("AUTORIZADOR")) {
            return "Acceso a Kardex para aprobar o rechazar solicitudes pendientes de inventario (RF-16).";
        }
        if (r.contains("TRANSPORTADOR")) {
            return "Acceso al módulo de Entregas asignadas para reportar salida a ruta y entrega final.";
        }
        return "Seleccione un módulo disponible.";
    }

    private void mostrarAyuda() {
        String msg = "MOBILIA HAUS - REGLAS DE NEGOCIO (RN-01 a RN-09)\n\n"
                + "• RN-01 (Regla de Oro): Todo producto nuevo inicia con Stock = 0. El stock jamás se edita directamente en el catálogo.\n"
                + "• RN-02 / RN-03: Las compras, devoluciones y ajustes físicos se solicitan en el Kardex como PENDIENTES.\n"
                + "• RN-04 / RN-05: Solo un Autorizador de Kardex o Administrador puede APROBAR o RECHAZAR movimientos pendientes.\n"
                + "• RN-06: Las Ventas validan disponibilidad antes de confirmar y descuentan el stock automáticamente en el Kardex.\n"
                + "• RN-07 / RN-08: Los Pagos descuentan el saldo pendiente con CTE atómica y las Entregas avanzan En bodega -> En ruta -> Entregado.";
        JOptionPane.showMessageDialog(this, msg, "Ayuda y Reglas de Negocio", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initComponents() {
        panelFondo = new JPanel();
        panelHeader = new JPanel();
        lblLogo = new JLabel();
        lblMarca = new JLabel();
        lblUsuario = new JLabel();
        lblBienvenida = new JLabel();
        lblRol = new JLabel();
        panelBotones = new JPanel();

        btnProductos = new JButton("Productos (Catálogo)");
        btnInventario = new JButton("Inventario y Kardex");
        btnClientes = new JButton("Clientes");
        btnProveedores = new JButton("Proveedores");
        btnVentas = new JButton("Ventas y Pagos");
        btnEntregas = new JButton("Entregas a Domicilio");
        btnUsuarios = new JButton("Usuarios y Roles");
        btnReportes = new JButton("Reportes Ejecutivos");
        btnAyuda = new JButton("Reglas de Negocio");
        btnCerrarSesion = new JButton("Cerrar sesión");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panelFondo.setBackground(EstiloGUI.FONDO);
        panelHeader.setBackground(EstiloGUI.CAFE_OSCURO);

        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("MH");

        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblMarca.setForeground(Color.WHITE);
        lblMarca.setText("Mobilia Haus");

        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUsuario.setText("Usuario");

        GroupLayout panelHeaderLayout = new GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblMarca)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                .addComponent(lblUsuario, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelHeaderLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMarca)
                    .addComponent(lblUsuario))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(EstiloGUI.CAFE_OSCURO);
        lblBienvenida.setText("Bienvenido");

        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(new Color(105, 105, 105));
        lblRol.setText("Seleccione un módulo");

        panelBotones.setBackground(EstiloGUI.FONDO);
        panelBotones.setLayout(new GridLayout(3, 4, 16, 16));

        btnProductos.addActionListener(e -> new VentanaProductosGUI(usuario, ctrlInventario).setVisible(true));
        btnInventario.addActionListener(e -> new VentanaInventarioGUI(usuario, ctrlInventario, ctrlProveedor).setVisible(true));
        btnClientes.addActionListener(e -> new VentanaClientesGUI(usuario, ctrlCliente).setVisible(true));
        btnProveedores.addActionListener(e -> new VentanaProveedoresGUI(usuario, ctrlProveedor).setVisible(true));
        btnVentas.addActionListener(e -> new VentanaVentasGUI(usuario, ctrlVenta, ctrlCliente, ctrlInventario).setVisible(true));
        btnEntregas.addActionListener(e -> new VentanaEntregasGUI(usuario, ctrlEntrega, ctrlAuth).setVisible(true));
        btnUsuarios.addActionListener(e -> new VentanaUsuariosGUI(usuario, ctrlAuth).setVisible(true));
        btnReportes.addActionListener(e -> new VentanaReportesGUI(usuario, ctrlVenta, ctrlInventario, ctrlCliente, ctrlEntrega).setVisible(true));
        btnAyuda.addActionListener(e -> mostrarAyuda());
        btnCerrarSesion.addActionListener(e -> {
            ctrlAuth.cerrarSesion();
            new LoginGUI(ctrlAuth).setVisible(true);
            dispose();
        });

        panelBotones.add(btnProductos);
        panelBotones.add(btnInventario);
        panelBotones.add(btnClientes);
        panelBotones.add(btnProveedores);
        panelBotones.add(btnVentas);
        panelBotones.add(btnEntregas);
        panelBotones.add(btnUsuarios);
        panelBotones.add(btnReportes);
        panelBotones.add(btnAyuda);
        panelBotones.add(btnCerrarSesion);

        GroupLayout panelFondoLayout = new GroupLayout(panelFondo);
        panelFondo.setLayout(panelFondoLayout);
        panelFondoLayout.setHorizontalGroup(
            panelFondoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelFondoLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(panelFondoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblBienvenida)
                    .addComponent(lblRol)
                    .addComponent(panelBotones, GroupLayout.PREFERRED_SIZE, 860, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        panelFondoLayout.setVerticalGroup(
            panelFondoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelFondoLayout.createSequentialGroup()
                .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(lblBienvenida)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRol)
                .addGap(24, 24, 24)
                .addComponent(panelBotones, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelFondo, BorderLayout.CENTER);
        pack();
    }
}
