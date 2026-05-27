package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.CategoriaRequestDTO;
import com.climatizacion.sistema_clima.dto.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO crearCategoria(CategoriaRequestDTO categoriaRequestDTO);
    List<CategoriaResponseDTO> obtenerTodas();
    CategoriaResponseDTO obtenerPorId(Long id);
    void eliminarCategoria(Long id);
}
