// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para gestion de Proveedores
package com.uniajc.controlador;

import com.uniajc.modelo.Proveedor;
import com.uniajc.servicios.ProveedorService;

import java.sql.SQLException;
import java.util.List;

public class ControladorProveedor {

    private final ProveedorService proveedorService;

    public ControladorProveedor() {
        this(new ProveedorService());
    }

    public ControladorProveedor(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    public boolean registrarProveedor(String nit, String razonSocial, String telefono, String correo) throws SQLException {
        Proveedor nuevo = new Proveedor(0, nit, razonSocial, telefono, correo);
        return proveedorService.registrarProveedor(nuevo);
    }

    public boolean actualizarProveedor(int idProveedor, String nit, String razonSocial, String telefono, String correo) throws SQLException {
        Proveedor prod = new Proveedor(idProveedor, nit, razonSocial, telefono, correo);
        return proveedorService.actualizarProveedor(prod);
    }

    public Proveedor buscarPorNit(String nit) throws SQLException {
        return proveedorService.buscarPorNit(nit);
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedorService.listarProveedores();
    }
}
