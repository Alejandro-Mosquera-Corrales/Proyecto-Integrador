// Autor: Jahel Gutiérrez | Commit 7: Pruebas unitarias de datos de Cliente y Proveedor (RF-03, RF-04)
package com.uniajc.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteYProveedorTest {

    @Test
    @DisplayName("Cliente: herencia de Persona y datos de contacto")
    public void testClienteDatos() {
        Cliente c = new Cliente(10, "Andres", "Lopez", "1144000111", "3158889900", "andres@correo.com", "Calle 10 # 5-20");

        assertEquals("Andres Lopez", c.getNombreCompleto());
        assertEquals("1144000111", c.getDocumento());
        assertEquals("Calle 10 # 5-20", c.getDireccion());
    }

    @Test
    @DisplayName("Proveedor: validación de NIT, razón social y contacto")
    public void testProveedorDatos() {
        Proveedor p = new Proveedor(5, "900123456-7", "Maderas del Valle S.A.S.", "3104445566", "contacto@maderasvalle.com");

        assertEquals(5, p.getIdProveedor());
        assertEquals("900123456-7", p.getNitDocumento());
        assertEquals("Maderas del Valle S.A.S.", p.getRazonSocial());
        assertTrue(p.toString().contains("Maderas del Valle S.A.S."));
    }
}
