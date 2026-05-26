package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.entities.CategoriaEntity;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
import com.climatizacion.sistema_clima.entities.ProductoImagen;
import com.climatizacion.sistema_clima.repository.CategoriaRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

        // Lógica para mapear y guardar las imágenes vinculadas a este producto
        if (dto.getImagenesUrls() != null && !dto.getImagenesUrls().isEmpty()) {
            List<ProductoImagen> imagenes = dto.getImagenesUrls().stream()
                    .map(url -> ProductoImagen.builder()
                            .imagenUrl(url)
                            .producto(producto) // Vinculamos al producto actual
                            .esPrincipal(dto.getImagenesUrls().indexOf(url) == 0) // La primera es principal
                            .build())
                    .collect(Collectors.toList());
            producto.getImagenes().addAll(imagenes);
        }

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

        // Actualización de imágenes: Limpiamos las actuales y agregamos las nuevas
        productoExistente.getImagenes().clear();
        if (dto.getImagenesUrls() != null && !dto.getImagenesUrls().isEmpty()) {
            List<ProductoImagen> nuevasImagenes = dto.getImagenesUrls().stream()
                    .map(url -> ProductoImagen.builder()
                            .imagenUrl(url)
                            .producto(productoExistente)
                            .esPrincipal(dto.getImagenesUrls().indexOf(url) == 0)
                            .build())
                    .collect(Collectors.toList());
            productoExistente.getImagenes().addAll(nuevasImagenes);
        }

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
        // Extraemos solo las URLs de las entidades ProductoImagen para enviar al Frontend
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
                .imagenesUrls(urls) // Pasamos la lista de URLs
                .build();
    }

    @Override
    @Transactional
    public ProductoResponseDTO crearConImagen(ProductoRequestDTO dto, MultipartFile archivoImagen) {
        // 1. Guardar los datos de texto (Igual que antes)
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

        // 2. Lógica para guardar el ARCHIVO FÍSICO
        if (archivoImagen != null && !archivoImagen.isEmpty()) {
            try {
                // Crear carpeta si no existe
                Path directorioUploads = Paths.get("uploads");
                if (!Files.exists(directorioUploads)) {
                    Files.createDirectories(directorioUploads);
                }

                // Generar un nombre único para la imagen para que no se sobrescriban
                String nombreOriginal = archivoImagen.getOriginalFilename();
                String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
                Path rutaArchivo = directorioUploads.resolve(nombreUnico);

                // Copiar el archivo del dispositivo a la carpeta del servidor
                Files.copy(archivoImagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                // Generar la URL pública y enlazarla a la base de datos
                String urlPublica = "/uploads/" + nombreUnico;

                ProductoImagen imagenDb = ProductoImagen.builder()
                        .imagenUrl(urlPublica)
                        .producto(producto)
                        .esPrincipal(true)
                        .build();

                producto.getImagenes().add(imagenDb);

            } catch (Exception e) {
                throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
            }
        }

        return mapearAResponseDTO(productoRepository.save(producto));
    }
}