package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.entities.CategoriaEntity;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
import com.climatizacion.sistema_clima.repository.CategoriaRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Error: La categoria con ID" + dto.getIdCategoria() + "no existe"));

        ProductoEntity producto = ProductoEntity.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .capacidadBtu(dto.getCapacidadBTU())
                .stock(dto.getStock())
                .categoria(categoria)
                .activo(true)
                .build();

        return mapearAResponseDTO(productoRepository.save(producto));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return mapearAResponseDTO(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        ProductoEntity productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar, el producto no existe"));

        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("La nueva categoria no existe"));

        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecio(dto.getPrecio());
        productoExistente.setCapacidadBtu(dto.getCapacidadBTU());
        productoExistente.setStock(dto.getStock());
        productoExistente.setCategoria(categoria);

        return mapearAResponseDTO(productoRepository.save(productoExistente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoResponseDTO mapearAResponseDTO(ProductoEntity entity){
        return ProductoResponseDTO.builder()
                .idProducto(entity.getIdProducto())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .precio(entity.getPrecio())
                .capacidadBTU(entity.getCapacidadBtu())
                .stock(entity.getStock())
                .activo(entity.getActivo())
                .idCategoria(entity.getCategoria().getIdCategoria())
                .nombreCategoria(entity.getCategoria().getNombre())
                .build();
    }
}
