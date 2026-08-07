package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
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
    Long obtenerStock(Long idProducto);
    Page<ProductoResponseDTO> obtenerProductosPaginados(String search, String categoria, Pageable pageable);
    Page<ProductoResponseDTO> listarConFiltros(String busqueda, String categoria, String marca,
                                               Double precioMin, Double precioMax,
                                               Integer btuMin, Integer btuMax, Pageable pageable);
}