// Autor: Jahel Gutiérrez | Commit 6: Punto de entrada principal con ensamblaje MVC (Inyeccion de dependencias)
package com.uniajc;

import com.uniajc.controlador.ControladorAuth;
import com.uniajc.dao.UsuarioDAO;
import com.uniajc.servicios.AuthService;
import com.uniajc.vista.EstiloGUI;
import com.uniajc.vista.LoginGUI;
import com.uniajc.vista.VistaConsola;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Ensamblaje de la arquitectura en capas MVC (DAO -> Servicio -> Controlador -> Vista)
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        AuthService authService = new AuthService(usuarioDAO);
        ControladorAuth controladorAuth = new ControladorAuth(authService);

        // Si se pasa el argumento --consola se lanza en modo terminal (Scanner);
        // de lo contrario inicia la interfaz gráfica Swing de Mobilia Haus.
        boolean modoConsola = args != null && args.length > 0 && "--consola".equalsIgnoreCase(args[0]);

        if (modoConsola) {
            VistaConsola vistaConsola = new VistaConsola(controladorAuth);
            vistaConsola.iniciar();
        } else {
            EstiloGUI.aplicarTemaGlobal();
            SwingUtilities.invokeLater(() -> {
                LoginGUI login = new LoginGUI(controladorAuth);
                login.setVisible(true);
            });
        }
    }
}
