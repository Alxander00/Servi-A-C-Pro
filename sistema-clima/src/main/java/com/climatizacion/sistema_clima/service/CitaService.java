package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import java.util.List;

public interface CitaService {
    List<CitaResponseDTO> obtenerTodas();
    List<CitaResponseDTO> obtenerPorTecnico(Long idTecnico);
    CitaResponseDTO crear(CitaRequestDTO request);
    CitaResponseDTO actualizar(Integer id, CitaRequestDTO request);
    void cambiarEstado(Integer id, String estado);
}