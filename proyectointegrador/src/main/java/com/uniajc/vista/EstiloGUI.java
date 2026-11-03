// Autor: Jahel Gutiérrez | Commit 6: Clase utilitaria de estilos visuales Swing (Paleta Mobilia Haus)
package com.uniajc.vista;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;

public class EstiloGUI {

    public static final Color CAFE_OSCURO = new Color(74, 39, 18);
    public static final Color CAFE_MEDIO = new Color(112, 70, 42);
    public static final Color FONDO = new Color(249, 247, 244);
    public static final Color BLANCO = Color.WHITE;
    public static final Color GRIS_BORDE = new Color(210, 205, 198);

    public static void aplicarTemaGlobal() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void ponerLogo(JLabel label, int ancho, int alto) {
        try {
            URL url = EstiloGUI.class.getResource("/logo_mobilia_haus.png");
            if (url != null) {
                ImageIcon icono = new ImageIcon(url);
                Image img = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
            } else {
                label.setText("MH");
                label.setFont(new Font("Segoe UI", Font.BOLD, 20));
            }
        } catch (Exception e) {
            label.setText("MH");
        }
    }

    public static void prepararBoton(JButton boton, boolean principal) {
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(principal ? CAFE_OSCURO : CAFE_MEDIO);
        boton.setForeground(Color.WHITE);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void configurarTabla(JTable tabla) {
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setSelectionBackground(new Color(224, 210, 198));
        tabla.setSelectionForeground( Color.BLACK );
        tabla.setGridColor(GRIS_BORDE);
        JTableHeader header = tabla.getTableHeader();
        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.setBackground(CAFE_OSCURO);
            header.setForeground(Color.WHITE);
        }
    }
}
