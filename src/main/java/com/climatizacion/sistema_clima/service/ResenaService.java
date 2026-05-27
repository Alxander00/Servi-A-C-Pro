package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.ResenaRequestDTO;
import com.climatizacion.sistema_clima.dto.ResenaResponseDTO;
import java.util.List;
import java.util.Map;

public interface ResenaService {
    ResenaResponseDTO crearResena(Long usuarioId, ResenaRequestDTO request);
    List<ResenaResponseDTO> listarResenasPorProducto(Long productoId);
    Map<String, Object> obtenerEstadisticas(Long productoId);
    boolean puedeResenar(Long usuarioId, Long productoId);
}