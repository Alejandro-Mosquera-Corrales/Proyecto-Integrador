// Autor: Jahel Gutiérrez | Commit 6: Ventana de inicio de sesion en Swing con estetica Mobilia Haus (RF-01)
package com.uniajc.vista;

import com.uniajc.controlador.ControladorAuth;
import com.uniajc.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {

    private final ControladorAuth controladorAuth;
    private JPanel panelFondo;
    private JPanel panelTarjeta;
    private JLabel lblLogo;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JLabel lblUsuario;
    private JTextField txtUsuario;
    private JLabel lblContrasena;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnConsola;
    private JButton btnSalir;
    private JLabel lblAyuda;

    public LoginGUI() {
        this(new ControladorAuth());
    }

    public LoginGUI(ControladorAuth controladorAuth) {
        this.controladorAuth = controladorAuth;
        initComponents();
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("Mobilia Haus - Inicio de sesión");
        setLocationRelativeTo(null);
        setResizable(false);
        EstiloGUI.ponerLogo(lblLogo, 175, 175);
        EstiloGUI.prepararBoton(btnIngresar, true);
        EstiloGUI.prepararBoton(btnConsola, false);
        EstiloGUI.prepararBoton(btnSalir, false);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void iniciarSesion() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtContrasena.getPassword()).trim();

        try {
            Usuario u = controladorAuth.iniciarSesion(user, pass);
            MenuPrincipalGUI menu = new MenuPrincipalGUI(u, controladorAuth);
            menu.setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Acceso denegado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModoConsola() {
        dispose();
        new Thread(() -> {
            VistaConsola consola = new VistaConsola(controladorAuth);
            consola.iniciar();
        }).start();
    }

    private void initComponents() {
        panelFondo = new JPanel();
        panelTarjeta = new JPanel();
        lblLogo = new JLabel();
        lblTitulo = new JLabel();
        lblSubtitulo = new JLabel();
        lblUsuario = new JLabel();
        txtUsuario = new JTextField();
        lblContrasena = new JLabel();
        txtContrasena = new JPasswordField();
        btnIngresar = new JButton();
        btnConsola = new JButton();
        btnSalir = new JButton();
        lblAyuda = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panelFondo.setBackground(EstiloGUI.FONDO);
        panelTarjeta.setBackground(EstiloGUI.BLANCO);
        panelTarjeta.setBorder(BorderFactory.createLineBorder(EstiloGUI.GRIS_BORDE));

        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("Mobilia Haus");

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);
        lblTitulo.setText("MOBILIA HAUS");

        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(110, 110, 110));
        lblSubtitulo.setText("Sistema de Gestión de Inventario (Kardex) y Ventas");

        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsuario.setForeground(EstiloGUI.CAFE_OSCURO);
        lblUsuario.setText("Usuario");

        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblContrasena.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContrasena.setForeground(EstiloGUI.CAFE_OSCURO);
        lblContrasena.setText("Contraseña");

        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnIngresar.setText("Ingresar");
        btnIngresar.addActionListener(evt -> iniciarSesion());

        btnConsola.setText("Modo Consola");
        btnConsola.addActionListener(evt -> abrirModoConsola());

        btnSalir.setText("Salir");
        btnSalir.addActionListener(evt -> System.exit(0));

        lblAyuda.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblAyuda.setForeground(new Color(120, 120, 120));
        lblAyuda.setText("Conectado a PostgreSQL (Neon) bajo arquitectura MVC");

        GroupLayout panelTarjetaLayout = new GroupLayout(panelTarjeta);
        panelTarjeta.setLayout(panelTarjetaLayout);
        panelTarjetaLayout.setHorizontalGroup(
            panelTarjetaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelTarjetaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(panelTarjetaLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitulo)
                    .addComponent(lblSubtitulo)
                    .addComponent(lblUsuario)
                    .addComponent(txtUsuario)
                    .addComponent(lblContrasena)
                    .addComponent(txtContrasena)
                    .addGroup(panelTarjetaLayout.createSequentialGroup()
                        .addComponent(btnIngresar, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnConsola, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSalir, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblAyuda, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        panelTarjetaLayout.setVerticalGroup(
            panelTarjetaLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelTarjetaLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(panelTarjetaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lblLogo, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelTarjetaLayout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo)
                        .addGap(18, 18, 18)
                        .addComponent(lblUsuario)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUsuario, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblContrasena)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtContrasena, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelTarjetaLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(btnIngresar, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConsola, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSalir, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblAyuda)))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        GroupLayout panelFondoLayout = new GroupLayout(panelFondo);
        panelFondo.setLayout(panelFondoLayout);
        panelFondoLayout.setHorizontalGroup(
            panelFondoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelFondoLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(panelTarjeta, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        panelFondoLayout.setVerticalGroup(
            panelFondoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelFondoLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(panelTarjeta, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelFondo, BorderLayout.CENTER);
        pack();
    }
}
