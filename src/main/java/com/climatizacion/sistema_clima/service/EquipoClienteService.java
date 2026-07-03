package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.EquipoClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.EquipoClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EquipoClienteService {
    List<EquipoClienteResponseDTO> obtenerTodos();
    List<EquipoClienteResponseDTO> obtenerPorCliente(Long idCliente);
    Page<EquipoClienteResponseDTO> obtenerPorClientePaginado(Long idCliente, Pageable pageable);
    EquipoClienteResponseDTO crear(EquipoClienteRequestDTO request);
    EquipoClienteResponseDTO actualizar(Long id, EquipoClienteRequestDTO request);
    void eliminar(Long id);
}