// Autor: Jahel Gutiérrez | Commit 4: Servicio de negocio para gestion de Clientes (RF-03)
package com.uniajc.servicios;

import com.uniajc.dao.ClienteDAO;
import com.uniajc.modelo.Cliente;

import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Valida y registra un nuevo cliente verificando unicidad de su documento (RF-03).
     */
    public boolean registrarCliente(Cliente cliente) throws SQLException {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (cliente.getDocumento() == null || cliente.getDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento de identificación del cliente es obligatorio.");
        }
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty() ||
            cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre y apellido del cliente son obligatorios.");
        }

        String docLimpio = cliente.getDocumento().trim();
        Cliente existente = clienteDAO.buscarPorDocumento(docLimpio);
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el documento: " + docLimpio);
        }

        cliente.setDocumento(docLimpio);
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setApellido(cliente.getApellido().trim());
        if (cliente.getTelefono() != null) cliente.setTelefono(cliente.getTelefono().trim());
        if (cliente.getCorreo() != null) cliente.setCorreo(cliente.getCorreo().trim());
        if (cliente.getDireccion() != null) cliente.setDireccion(cliente.getDireccion().trim());

        return clienteDAO.insertar(cliente);
    }

    public Cliente buscarPorDocumento(String documento) throws SQLException {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el documento a buscar.");
        }
        return clienteDAO.buscarPorDocumento(documento.trim());
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        if (idCliente <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido.");
        }
        return clienteDAO.buscarPorId(idCliente);
    }

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listarTodos();
    }
}
