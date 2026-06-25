package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);

    // ✅ CAMBIO APLICADO: Ahora retorna un Page
    Page<ProductoResponseDTO> listarActivos(Pageable pageable);

    ProductoResponseDTO obtenerPorId(Long idProducto);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void eliminar(Long idProducto);
    ProductoResponseDTO crearConImagenes(ProductoRequestDTO dto, List<MultipartFile> imagenes);
    ProductoResponseDTO actualizarConImagenes(Long id, ProductoRequestDTO dto, List<MultipartFile> nuevasImagenes);
    List<ProductoResponseDTO> listarActivosOrdenadosPorPopularidad();
}