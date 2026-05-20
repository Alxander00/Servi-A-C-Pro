package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.DetalleCitaRequestDTO;
import com.climatizacion.sistema_clima.dto.DetalleCitaResponseDTO;
import java.util.List;

public interface DetalleCitaService {
    List<DetalleCitaResponseDTO> obtenerPorCita(Integer idCita);
    DetalleCitaResponseDTO crear(DetalleCitaRequestDTO request);
    void eliminar(Integer idDetalleCita);
}