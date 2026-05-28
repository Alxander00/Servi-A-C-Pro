package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.entities.CategoriaEntity;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
import com.climatizacion.sistema_clima.entities.ProductoImagen;
import com.climatizacion.sistema_clima.repository.CategoriaRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.service.CloudinaryService;
import com.climatizacion.sistema_clima.service.HistorialPrecioService;
import com.climatizacion.sistema_clima.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HistorialPrecioService historialPrecioService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        // ... (sin cambios, igual que antes)
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));
        ProductoEntity producto = ProductoEntity.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .capacidadBtu(dto.getCapacidadBTU())
                .stock(dto.getStock())
                .categoria(categoria)
                .activo(true)
                .build();
        if (dto.getImagenesUrls() != null && !dto.getImagenesUrls().isEmpty()) {
            List<ProductoImagen> imagenes = dto.getImagenesUrls().stream()
                    .map(url -> ProductoImagen.builder().imagenUrl(url).producto(producto).esPrincipal(dto.getImagenesUrls().indexOf(url) == 0).build())
                    .collect(Collectors.toList());
            producto.getImagenes().addAll(imagenes);
        }
        ProductoEntity guardado = productoRepository.save(producto);
        historialPrecioService.registrarCambioPrecio(guardado.getIdProducto(), guardado.getPrecio());
        return mapearAResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByActivoTrue().stream().map(this::mapearAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        ProductoEntity producto = productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return mapearAResponseDTO(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        // Delegamos al nuevo método sin imágenes
        return actualizarConImagenes(id, dto, null);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizarConImagenes(Long id, ProductoRequestDTO dto, List<MultipartFile> nuevasImagenes) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        BigDecimal precioAnterior = producto.getPrecio();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCapacidadBtu(dto.getCapacidadBTU());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        // Manejo de imágenes: si vienen nuevas, reemplazar
        if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
            // Opcional: eliminar imágenes antiguas de Cloudinary (no implementado aquí, pero se puede)
            producto.getImagenes().clear();
            for (MultipartFile img : nuevasImagenes) {
                try {
                    String urlPublica = cloudinaryService.subirImagen(img);
                    ProductoImagen imagenEntity = ProductoImagen.builder()
                            .imagenUrl(urlPublica)
                            .producto(producto)
                            .esPrincipal(producto.getImagenes().isEmpty())
                            .build();
                    producto.getImagenes().add(imagenEntity);
                } catch (IOException e) {
                    throw new RuntimeException("Error subiendo imagen a Cloudinary: " + e.getMessage());
                }
            }
        } else if (dto.getImagenesUrls() != null && !dto.getImagenesUrls().isEmpty()) {
            // Mantener las URLs existentes (caso de que no se suban nuevas)
            producto.getImagenes().clear();
            for (String url : dto.getImagenesUrls()) {
                ProductoImagen imagenEntity = ProductoImagen.builder()
                        .imagenUrl(url)
                        .producto(producto)
                        .esPrincipal(producto.getImagenes().isEmpty())
                        .build();
                producto.getImagenes().add(imagenEntity);
            }
        }

        ProductoEntity actualizado = productoRepository.save(producto);
        if (precioAnterior.compareTo(dto.getPrecio()) != 0) {
            historialPrecioService.registrarCambioPrecio(id, dto.getPrecio());
        }
        return mapearAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO crearConImagenes(ProductoRequestDTO dto, List<MultipartFile> imagenes) {
        // Igual que ya tienes, sin cambios
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));
        ProductoEntity producto = ProductoEntity.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .capacidadBtu(dto.getCapacidadBTU())
                .stock(dto.getStock())
                .categoria(categoria)
                .activo(true)
                .build();
        ProductoEntity productoGuardado = productoRepository.save(producto);
        if (imagenes != null && !imagenes.isEmpty()) {
            for (MultipartFile img : imagenes) {
                try {
                    String urlPublica = cloudinaryService.subirImagen(img);
                    ProductoImagen imagenEntity = ProductoImagen.builder()
                            .imagenUrl(urlPublica)
                            .producto(productoGuardado)
                            .esPrincipal(imagenes.indexOf(img) == 0)
                            .build();
                    productoGuardado.getImagenes().add(imagenEntity);
                } catch (IOException e) {
                    throw new RuntimeException("Error subiendo imagen a Cloudinary: " + e.getMessage());
                }
            }
            productoGuardado = productoRepository.save(productoGuardado);
        }
        historialPrecioService.registrarCambioPrecio(productoGuardado.getIdProducto(), productoGuardado.getPrecio());
        return mapearAResponseDTO(productoGuardado);
    }

    @Override
    public List<ProductoResponseDTO> listarActivosOrdenadosPorPopularidad() {
        List<Object[]> resultados = productoRepository.findProductosConVentas();
        return resultados.stream().map(row -> {
            ProductoEntity p = (ProductoEntity) row[0];
            Long vendido = ((Number) row[1]).longValue();
            ProductoResponseDTO dto = mapearAResponseDTO(p);
            dto.setTotalVendido(vendido);
            return dto;
        }).collect(Collectors.toList());
    }

    private ProductoResponseDTO mapearAResponseDTO(ProductoEntity entity) {
        List<String> urls = entity.getImagenes() != null
                ? entity.getImagenes().stream().map(ProductoImagen::getImagenUrl).collect(Collectors.toList())
                : new ArrayList<>();
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
                .imagenesUrls(urls)
                .build();
    }
}