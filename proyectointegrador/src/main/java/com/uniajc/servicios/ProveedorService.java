// Autor: Jahel Gutiérrez | Commit 4: Servicio de negocio para gestion de Proveedores (RF-04)
package com.uniajc.servicios;

import com.uniajc.dao.ProveedorDAO;
import com.uniajc.modelo.Proveedor;

import java.sql.SQLException;
import java.util.List;

public class ProveedorService {

    private final ProveedorDAO proveedorDAO;

    public ProveedorService() {
        this(new ProveedorDAO());
    }

    public ProveedorService(ProveedorDAO proveedorDAO) {
        this.proveedorDAO = proveedorDAO;
    }

    /**
     * Registra un proveedor validando NIT único y razón social obligatoria (RF-04).
     */
    public boolean registrarProveedor(Proveedor proveedor) throws SQLException {
        if (proveedor == null) {
            throw new IllegalArgumentException("Los datos del proveedor son obligatorios.");
        }
        if (proveedor.getNitDocumento() == null || proveedor.getNitDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT o documento del proveedor es obligatorio.");
        }
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("La razón social del proveedor es obligatoria.");
        }

        String nitLimpio = proveedor.getNitDocumento().trim();
        Proveedor existente = proveedorDAO.buscarPorNit(nitLimpio);
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con el NIT: " + nitLimpio);
        }

        proveedor.setNitDocumento(nitLimpio);
        proveedor.setRazonSocial(proveedor.getRazonSocial().trim());
        if (proveedor.getTelefono() != null) proveedor.setTelefono(proveedor.getTelefono().trim());
        if (proveedor.getCorreo() != null) proveedor.setCorreo(proveedor.getCorreo().trim());

        return proveedorDAO.insertar(proveedor);
    }

    public boolean actualizarProveedor(Proveedor proveedor) throws SQLException {
        if (proveedor == null || proveedor.getIdProveedor() <= 0) {
            throw new IllegalArgumentException("Seleccione un proveedor válido para actualizar.");
        }
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("La razón social del proveedor es obligatoria.");
        }
        return proveedorDAO.actualizar(proveedor);
    }

    public Proveedor buscarPorNit(String nit) throws SQLException {
        if (nit == null || nit.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el NIT del proveedor.");
        }
        return proveedorDAO.buscarPorNit(nit.trim());
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedorDAO.listarTodos();
    }
}
