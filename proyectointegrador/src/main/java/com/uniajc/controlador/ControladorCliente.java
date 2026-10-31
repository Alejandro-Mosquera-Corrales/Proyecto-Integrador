// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para gestion de Clientes
package com.uniajc.controlador;

import com.uniajc.modelo.Cliente;
import com.uniajc.servicios.ClienteService;

import java.sql.SQLException;
import java.util.List;

public class ControladorCliente {

    private final ClienteService clienteService;

    public ControladorCliente() {
        this(new ClienteService());
    }

    public ControladorCliente(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public boolean registrarCliente(String nombre, String apellido, String documento,
                                    String telefono, String correo, String direccion) throws SQLException {
        Cliente nuevo = new Cliente(0, nombre, apellido, documento, telefono, correo, direccion);
        return clienteService.registrarCliente(nuevo);
    }

    public boolean registrarCliente(Cliente cliente) throws SQLException {
        return clienteService.registrarCliente(cliente);
    }

    public Cliente buscarPorDocumento(String documento) throws SQLException {
        return clienteService.buscarPorDocumento(documento);
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        return clienteService.buscarPorId(idCliente);
    }

    public List<Cliente> listarClientes() throws SQLException {
        return clienteService.listarClientes();
    }
}
