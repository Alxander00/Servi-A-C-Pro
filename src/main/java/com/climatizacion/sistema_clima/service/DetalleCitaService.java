package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.DetalleCitaRequestDTO;
import com.climatizacion.sistema_clima.dto.DetalleCitaResponseDTO;
import java.util.List;

public interface DetalleCitaService {
    List<DetalleCitaResponseDTO> obtenerPorCita(Long idCita);
    DetalleCitaResponseDTO crear(DetalleCitaRequestDTO request);
    void eliminar(Long idDetalleCita);
}