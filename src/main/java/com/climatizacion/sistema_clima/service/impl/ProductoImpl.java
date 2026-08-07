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
import com.climatizacion.sistema_clima.specification.ProductoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de productos (equipos de aire acondicionado).
 */
@Service
@RequiredArgsConstructor
public class ProductoImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HistorialPrecioService historialPrecioService;
    private final CloudinaryService cloudinaryService;

    /**
     * Crea un nuevo producto sin imágenes (solo datos básicos).
     * @param dto Datos del producto
     * @return DTO con los datos del producto creado
     */
    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        return crearConImagenes(dto, null);
    }

    /**
     * Lista productos activos con paginación.
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listarActivos(Pageable pageable) {
        return productoRepository.findByActivoTrue(pageable)
                .map(this::mapearAResponseDTO);
    }

    /**
     * Obtiene un producto por su ID.
     * @param idProducto ID del producto
     * @return DTO del producto
     * @throws RuntimeException si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long idProducto) {
        ProductoEntity producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return mapearAResponseDTO(producto);
    }

    /**
     * Actualiza un producto sin imágenes (solo datos básicos).
     */
    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        return actualizarConImagenes(id, dto, null);
    }

    /**
     * Actualiza un producto permitiendo añadir o reemplazar imágenes.
     * @param id ID del producto
     * @param dto Datos del producto
     * @param nuevasImagenes Nuevas imágenes a añadir
     * @return DTO con los datos actualizados
     */
    @Override
    @Transactional
    public ProductoResponseDTO actualizarConImagenes(Long id, ProductoRequestDTO dto, List<MultipartFile> nuevasImagenes) {
        // Validar existencia
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // ✅ Validar categoría
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        // ✅ Validar precio
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }

        // ✅ Validar stock
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        BigDecimal precioAnterior = producto.getPrecio();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCapacidadBtu(dto.getCapacidadBTU());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        // Manejo de imágenes
        if (dto.getImagenesUrls() != null) {
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

        if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
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
        }

        ProductoEntity actualizado = productoRepository.save(producto);

        if (precioAnterior.compareTo(dto.getPrecio()) != 0) {
            historialPrecioService.registrarCambioPrecio(id, dto.getPrecio());
        }

        return mapearAResponseDTO(actualizado);
    }

    /**
     * Elimina un producto (soft delete - lo marca como inactivo).
     * @param idProducto ID del producto
     */
    @Override
    @Transactional
    public void eliminar(Long idProducto) {
        ProductoEntity producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    /**
     * Crea un producto con imágenes.
     * @param dto Datos del producto
     * @param imagenes Lista de archivos de imagen
     * @return DTO con los datos del producto creado
     */
    @Override
    @Transactional
    public ProductoResponseDTO crearConImagenes(ProductoRequestDTO dto, List<MultipartFile> imagenes) {
        // ✅ Validar categoría
        CategoriaEntity categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        // ✅ Validar precio
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }

        // ✅ Validar stock
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

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
            for (int i = 0; i < imagenes.size(); i++) {
                MultipartFile img = imagenes.get(i);
                try {
                    String urlPublica = cloudinaryService.subirImagen(img);
                    ProductoImagen imagenEntity = ProductoImagen.builder()
                            .imagenUrl(urlPublica)
                            .producto(productoGuardado)
                            .esPrincipal(i == 0)
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

    /**
     * Lista productos activos ordenados por popularidad (más vendidos).
     * @return Lista de productos ordenados por ventas
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * Obtiene el stock disponible de un producto.
     * @param idProducto ID del producto
     * @return Cantidad en stock
     */
    @Override
    @Transactional(readOnly = true)
    public Long obtenerStock(Long idProducto) {
        ProductoEntity producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return producto.getStock();
    }

    /**
     * Lista productos con filtros de búsqueda y categoría, paginados.
     * @param search Texto de búsqueda (nombre del producto)
     * @param categoria ID de la categoría (opcional)
     * @param pageable Configuración de paginación
     * @return Página de productos filtrados
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> obtenerProductosPaginados(String search, String categoria, Pageable pageable) {
        return productoRepository.buscarActivosConFiltros(search, categoria, pageable)
                .map(this::mapearAResponseDTO);
    }

    // ===== MÉTODOS PRIVADOS =====

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

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listarConFiltros(String busqueda, String categoria, String marca,
                                                      Double precioMin, Double precioMax,
                                                      Integer btuMin, Integer btuMax, Pageable pageable) {
        Specification<ProductoEntity> spec = ProductoSpecification.conFiltros(
                busqueda, categoria, marca, precioMin, precioMax, btuMin, btuMax);
        Page<ProductoEntity> pagina = productoRepository.findAll(spec, pageable);
        return pagina.map(this::mapearAResponseDTO);
    }
}