package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    List<ProductoResponseDTO> listarActivos();
    ProductoResponseDTO obtenerPorId(Long idProducto);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void eliminar(Long idProducto);
    ProductoResponseDTO crearConImagenes(ProductoRequestDTO dto, List<MultipartFile> imagenes);
    List<ProductoResponseDTO> listarActivosOrdenadosPorPopularidad();
}
