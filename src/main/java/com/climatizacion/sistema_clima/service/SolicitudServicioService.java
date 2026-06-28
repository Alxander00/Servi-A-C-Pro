package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudServicioService {
    SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request);
    List<SolicitudResponseDTO> listarSolicitudesPendientes();
    List<SolicitudResponseDTO> listarPorCliente(Long idCliente);
    void asignarTecnico(Long idSolicitud, Long idTecnico, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    void rechazarSolicitud(Long idSolicitud);
    long contarPendientes();
    long contarPendientesPorCliente(Long idCliente);
}