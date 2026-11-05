// Autor: Jahel Gutiérrez | Commit 7: Pruebas unitarias de Persona, Usuario y permisos de Rol (RF-01, RF-02, RN-09)
package com.uniajc.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    @DisplayName("Herencia de Persona: Usuario debe exponer correctamente nombre completo y datos base")
    public void testHerenciaPersona() {
        Rol rolVendedor = new Rol(2, "VENDEDOR", "Atención comercial");
        Usuario u = new Usuario(1, "Carlos", "Perez", "carlos@mobilia.com", "3001234567",
                "cperez", "hash123", rolVendedor, EstadoUsuario.ACTIVO);

        assertEquals("Carlos Perez", u.getNombreCompleto());
        assertTrue(u.estaActivo());
    }

    @Test
    @DisplayName("Validación de estado activo: usuario inactivo no debe tener permisos (RN-09)")
    public void testUsuarioInactivoSinPermisos() {
        Rol rolAdmin = new Rol(1, "ADMINISTRADOR", "Acceso total");
        Usuario u = new Usuario(2, "Maria", "Gomez", "maria@mobilia.com", "3007654321",
                "mgomez", "hash456", rolAdmin, EstadoUsuario.INACTIVO);

        assertFalse(u.estaActivo(), "El usuario debe reportar inactivo");
        assertFalse(u.tienePermiso("CREAR", "VENTAS"), "Un usuario inactivo no puede operar");
        assertFalse(u.tienePermiso("CONSULTAR", "PRODUCTOS"), "Un usuario inactivo no puede consultar");
    }

    @Test
    @DisplayName("Matriz de permisos por Rol: Vendedor vs Administrador")
    public void testPermisosRol() {
        Rol rolAdmin = new Rol(1, "ADMINISTRADOR", "Acceso total");
        Rol rolVendedor = new Rol(2, "VENDEDOR", "Comercial");
        Rol rolTransportador = new Rol(5, "TRANSPORTADOR", "Logística");

        Usuario admin = new Usuario(1, "Admin", "General", "admin@mobilia.com", "3111111111", "admin", "p1", rolAdmin, EstadoUsuario.ACTIVO);
        Usuario vendedor = new Usuario(2, "Juan", "Ventas", "juan@mobilia.com", "3222222222", "juanv", "p2", rolVendedor, EstadoUsuario.ACTIVO);
        Usuario transportador = new Usuario(3, "Pedro", "Ruta", "pedro@mobilia.com", "3333333333", "pedrot", "p3", rolTransportador, EstadoUsuario.ACTIVO);

        // Administrador tiene acceso a crear
        assertTrue(admin.tienePermiso("CREAR", "PRODUCTOS"));
        assertTrue(admin.tienePermiso("ELIMINAR", "USUARIOS"));

        // Vendedor puede crear ventas y clientes, pero no productos ni eliminar
        assertTrue(vendedor.tienePermiso("CREAR", "VENTAS"));
        assertTrue(vendedor.tienePermiso("CREAR", "CLIENTES"));
        assertFalse(vendedor.tienePermiso("CREAR", "PRODUCTOS"));
        assertFalse(vendedor.tienePermiso("ELIMINAR", "USUARIOS"));

        // Transportador solo gestiona entregas
        assertTrue(transportador.tienePermiso("ACTUALIZAR", "ENTREGAS"));
        assertFalse(transportador.tienePermiso("CREAR", "VENTAS"));
    }
}
