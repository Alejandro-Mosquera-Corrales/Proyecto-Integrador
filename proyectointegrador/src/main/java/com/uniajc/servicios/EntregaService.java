// Autor: Jahel Gutiérrez | Commit 4: Servicio de Entregas y despachos a domicilio (RF-19, RF-20, RF-21, RN-07, RN-08)
package com.uniajc.servicios;

import com.uniajc.dao.EntregaDAO;
import com.uniajc.dao.VentaDAO;
import com.uniajc.modelo.Entrega;
import com.uniajc.modelo.EstadoEntrega;
import com.uniajc.modelo.Usuario;
import com.uniajc.modelo.Venta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EntregaService {

    private final EntregaDAO entregaDAO;
    private final VentaDAO ventaDAO;

    public EntregaService() {
        this(new EntregaDAO(), new VentaDAO());
    }

    public EntregaService(EntregaDAO entregaDAO, VentaDAO ventaDAO) {
        this.entregaDAO = entregaDAO;
        this.ventaDAO = ventaDAO;
    }

    /**
     * Registra y asigna una nueva entrega a domicilio en estado inicial EN_BODEGA (RF-19).
     */
    public boolean programarEntrega(Entrega entrega) throws SQLException {
        if (entrega == null) {
            throw new IllegalArgumentException("Los datos de la entrega son obligatorios.");
        }
        if (entrega.getIdVenta() <= 0) {
            throw new IllegalArgumentException("Debe indicar el número de venta asociada a la entrega.");
        }
        if (entrega.getDireccionEntrega() == null || entrega.getDireccionEntrega().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria.");
        }
        if (entrega.getNombreRecibe() == null || entrega.getNombreRecibe().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la persona que recibe es obligatorio.");
        }
        if (entrega.getTransportador() == null || entrega.getTransportador().getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Debe asignar un transportador responsable.");
        }

        Venta venta = ventaDAO.buscarPorId(entrega.getIdVenta());
        if (venta == null) {
            throw new IllegalArgumentException("La venta #" + entrega.getIdVenta() + " no existe en el sistema.");
        }

        if (entrega.getFechaProgramada() == null) {
            entrega.setFechaProgramada(LocalDateTime.now().plusDays(1));
        }
        entrega.setEstado(EstadoEntrega.EN_BODEGA);
        return entregaDAO.insertar(entrega);
    }

    /**
     * Avanza el estado logístico respetando el flujo secuencial EN_BODEGA -> EN_RUTA -> ENTREGADO (RF-21, RN-08)
     * y verificando que no se vuelva a descontar inventario en ningún caso.
     */
    public boolean actualizarEstadoEntrega(int idEntrega, Usuario ejecutor, EstadoEntrega nuevoEstado, String observacion) throws SQLException {
        if (idEntrega <= 0 || nuevoEstado == null) {
            throw new IllegalArgumentException("Datos de entrega o estado inválidos.");
        }
        Entrega actual = entregaDAO.buscarPorId(idEntrega);
        if (actual == null) {
            throw new IllegalArgumentException("La entrega #" + idEntrega + " no existe.");
        }

        EstadoEntrega estadoActual = actual.getEstado();
        if (estadoActual == EstadoEntrega.ENTREGADO) {
            throw new IllegalStateException("La entrega #" + idEntrega + " ya fue ENTREGADA y no admite cambios (RN-08).");
        }

        if (estadoActual == EstadoEntrega.EN_BODEGA && nuevoEstado != EstadoEntrega.EN_RUTA) {
            throw new IllegalArgumentException("Transición inválida (RN-08): Desde 'En bodega' solo puede pasar a 'En ruta'.");
        }
        if (estadoActual == EstadoEntrega.EN_RUTA && nuevoEstado != EstadoEntrega.ENTREGADO) {
            throw new IllegalArgumentException("Transición inválida (RN-08): Desde 'En ruta' solo puede pasar a 'Entregado'.");
        }

        int idTransportadorAsignado = actual.getTransportador().getIdUsuario();
        if (ejecutor != null && ejecutor.getRol() != null && ejecutor.getRol().getNombreRol() != null) {
            String rol = ejecutor.getRol().getNombreRol().toUpperCase();
            if (rol.contains("TRANSPORTADOR") && ejecutor.getIdUsuario() != idTransportadorAsignado) {
                throw new SecurityException("Solo el transportador asignado a esta entrega puede cambiar su estado (RF-20, RF-21).");
            }
        }

        return entregaDAO.actualizarEstado(idEntrega, idTransportadorAsignado, estadoActual, nuevoEstado, observacion);
    }

    /**
     * Lista las entregas filtrando por transportador si el usuario tiene rol Transportador,
     * o todas si es Administrador, Vendedor o Gerente/Auditor (RF-20).
     */
    public List<Entrega> listarEntregasPorUsuario(Usuario usuario) throws SQLException {
        if (usuario != null && usuario.getRol() != null && usuario.getRol().getNombreRol() != null) {
            String rol = usuario.getRol().getNombreRol().toUpperCase();
            if (rol.contains("TRANSPORTADOR")) {
                return entregaDAO.listarPorTransportador(usuario.getIdUsuario());
            }
        }
        return entregaDAO.listarPorTransportador(0);
    }

    public Entrega buscarPorId(int idEntrega) throws SQLException {
        return entregaDAO.buscarPorId(idEntrega);
    }
}
