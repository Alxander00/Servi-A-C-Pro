package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.EquipoClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.EquipoClienteResponseDTO;
import java.util.List;

public interface EquipoClienteService {
    List<EquipoClienteResponseDTO> obtenerTodos();
    List<EquipoClienteResponseDTO> obtenerPorCliente(Long idCliente);
    EquipoClienteResponseDTO crear(EquipoClienteRequestDTO request);
    EquipoClienteResponseDTO actualizar(Integer id, EquipoClienteRequestDTO request);
    void eliminar(Integer id);
}