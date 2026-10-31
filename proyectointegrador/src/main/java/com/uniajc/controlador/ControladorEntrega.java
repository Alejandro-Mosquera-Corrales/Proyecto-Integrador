// Autor: Jahel Gutiérrez | Commit 5: Controlador MVC para programacion y seguimiento de Entregas
package com.uniajc.controlador;

import com.uniajc.modelo.Entrega;
import com.uniajc.modelo.EstadoEntrega;
import com.uniajc.modelo.Usuario;
import com.uniajc.servicios.EntregaService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorEntrega {

    private final EntregaService entregaService;

    public ControladorEntrega() {
        this(new EntregaService());
    }

    public ControladorEntrega(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    public boolean programarEntrega(int idVenta, String direccion, String nombreRecibe,
                                    String docRecibe, String telRecibe, LocalDateTime fechaProg,
                                    String observacion, Usuario transportador) throws SQLException {
        Entrega e = new Entrega();
        e.setIdVenta(idVenta);
        e.setDireccionEntrega(direccion);
        e.setNombreRecibe(nombreRecibe);
        e.setDocumentoRecibe(docRecibe);
        e.setTelefonoRecibe(telRecibe);
        e.setFechaProgramada(fechaProg != null ? fechaProg : LocalDateTime.now().plusDays(1));
        e.setObservacion(observacion);
        e.setTransportador(transportador);
        return entregaService.programarEntrega(e);
    }

    public boolean avanzarEstadoEntrega(int idEntrega, Usuario ejecutor, EstadoEntrega nuevoEstado, String observacion) throws SQLException {
        return entregaService.actualizarEstadoEntrega(idEntrega, ejecutor, nuevoEstado, observacion);
    }

    public List<Entrega> listarEntregasPorUsuario(Usuario usuario) throws SQLException {
        return entregaService.listarEntregasPorUsuario(usuario);
    }

    public Entrega buscarPorId(int idEntrega) throws SQLException {
        return entregaService.buscarPorId(idEntrega);
    }
}
