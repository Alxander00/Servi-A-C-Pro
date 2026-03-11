package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    List<ProductoResponseDTO> listarActivos();
    ProductoResponseDTO obtenerPorId(Long idProducto);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void eliminar(Long idProducto);
}
