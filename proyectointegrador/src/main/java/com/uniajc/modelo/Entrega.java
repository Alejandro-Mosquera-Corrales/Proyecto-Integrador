// Autor: Alejandro Mosquera | Commit 2: Entidad Entrega para seguimiento logistico de despachos
package com.uniajc.modelo;

import java.time.LocalDateTime;

public class Entrega {

    private int idEntrega;
    private String direccionEntrega;
    private String nombreRecibe;
    private String documentoRecibe;
    private String telefonoRecibe;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaSalidaBodega;
    private LocalDateTime fechaEntrega;
    private String observacion;
    private int idVenta;
    private Usuario transportador;
    private EstadoEntrega estado;

    public Entrega() {
        this.estado = EstadoEntrega.EN_BODEGA;
    }

    public boolean esTransicionValida(EstadoEntrega nuevoEstado) {
        if (nuevoEstado == null || this.estado == null) return false;
        if (this.estado == EstadoEntrega.EN_BODEGA && nuevoEstado == EstadoEntrega.EN_RUTA) return true;
        if (this.estado == EstadoEntrega.EN_RUTA && nuevoEstado == EstadoEntrega.ENTREGADO) return true;
        return false;
    }

    public int getIdEntrega() { return idEntrega; }
    public void setIdEntrega(int idEntrega) { this.idEntrega = idEntrega; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getNombreRecibe() { return nombreRecibe; }
    public void setNombreRecibe(String nombreRecibe) { this.nombreRecibe = nombreRecibe; }

    public String getDocumentoRecibe() { return documentoRecibe; }
    public void setDocumentoRecibe(String documentoRecibe) { this.documentoRecibe = documentoRecibe; }

    public String getTelefonoRecibe() { return telefonoRecibe; }
    public void setTelefonoRecibe(String telefonoRecibe) { this.telefonoRecibe = telefonoRecibe; }

    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

    public LocalDateTime getFechaSalidaBodega() { return fechaSalidaBodega; }
    public void setFechaSalidaBodega(LocalDateTime fechaSalidaBodega) { this.fechaSalidaBodega = fechaSalidaBodega; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public Usuario getTransportador() { return transportador; }
    public void setTransportador(Usuario transportador) { this.transportador = transportador; }

    public EstadoEntrega getEstado() { return estado; }
    public void setEstado(EstadoEntrega estado) { this.estado = estado; }
}
