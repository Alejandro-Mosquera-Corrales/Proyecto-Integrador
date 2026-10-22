// Autor: Alejandro Mosquera | Commit 1: Conexion Singleton JDBC hacia PostgreSQL (Neon)
package com.uniajc.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionPostgresDatabase {

    private static ConexionPostgresDatabase instancia;
    private Connection conexion;
    private String url;
    private String user;
    private String password;

    private ConexionPostgresDatabase() {
        inicializarCredenciales();
    }

    private void inicializarCredenciales() {
        Properties prop = new Properties();
        try (InputStream in = ConexionPostgresDatabase.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                prop.load(in);
            } else {
                try (InputStream fallback = ConexionPostgresDatabase.class.getClassLoader().getResourceAsStream("config.example.properties")) {
                    if (fallback != null) {
                        prop.load(fallback);
                    }
                }
            }
            this.url = prop.getProperty("db.url", "jdbc:postgresql://localhost:5432/Mobilia-Haus");
            this.user = prop.getProperty("db.user", "postgres");
            this.password = prop.getProperty("db.password", "postgres");
            Class.forName("org.postgresql.Driver");
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("[MobiliaHaus-Config] Error al cargar driver o config.properties: " + ex.getMessage());
        }
    }

    public static synchronized ConexionPostgresDatabase getInstancia() {
        if (instancia == null) {
            instancia = new ConexionPostgresDatabase();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(this.url, this.user, this.password);
        }
        return conexion;
    }

    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException ex) {
                System.err.println("[MobiliaHaus-Config] Error al cerrar conexion: " + ex.getMessage());
            }
        }
    }
}
