package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.HistorialPrecioDTO;
import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.service.HistorialPrecioService;
import com.climatizacion.sistema_clima.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de equipos de aire acondicionado")
public class ProductoController {

    private final ProductoService productoService;
    private final HistorialPrecioService historialPrecioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Crear producto", description = "Registra un nuevo equipo con imágenes. Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado (solo ADMIN)")
    })
    public ResponseEntity<ProductoResponseDTO> crear(
            @Parameter(description = "Datos del producto en formato JSON") @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @Parameter(description = "Archivos de imagen (opcional)") @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        ProductoResponseDTO response = productoService.crearConImagenes(dto, imagenes);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar producto con imágenes", description = "Actualiza un equipo y sus imágenes. Requiere ADMIN.")
    public ResponseEntity<ProductoResponseDTO> actualizarConImagenes(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Datos del producto en JSON") @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @Parameter(description = "Nuevas imágenes (opcional)") @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        ProductoResponseDTO response = productoService.actualizarConImagenes(id, dto, imagenes);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar producto (solo datos)", description = "Actualiza un equipo sin modificar imágenes. Requiere ADMIN.")
    public ResponseEntity<ProductoResponseDTO> actualizarJson(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @GetMapping
    @Operation(summary = "Listar productos con filtros", description = "Obtiene una lista paginada de productos activos con múltiples filtros.")
    public ResponseEntity<Page<ProductoResponseDTO>> listarActivos(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Texto de búsqueda (nombre o descripción)") @RequestParam(required = false) String busqueda,
            @Parameter(description = "Nombre de la categoría") @RequestParam(required = false) String categoria,
            @Parameter(description = "Marca del equipo") @RequestParam(required = false) String marca,
            @Parameter(description = "Precio mínimo") @RequestParam(required = false) Double precioMin,
            @Parameter(description = "Precio máximo") @RequestParam(required = false) Double precioMax,
            @Parameter(description = "BTU mínimo") @RequestParam(required = false) Integer btuMin,
            @Parameter(description = "BTU máximo") @RequestParam(required = false) Integer btuMax,
            @Parameter(description = "Campo de ordenación (ej. precio, capacidadBtu)") @RequestParam(defaultValue = "idProducto") String orden,
            @Parameter(description = "Dirección: ASC o DESC") @RequestParam(defaultValue = "ASC") String direccion) {

        Sort.Direction dir = Sort.Direction.fromOptionalString(direccion).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, orden));

        Page<ProductoResponseDTO> pagina = productoService.listarConFiltros(
                busqueda, categoria, marca, precioMin, precioMax, btuMin, btuMax, pageable);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles de un producto específico.")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Eliminar producto (soft delete)", description = "Marca el producto como inactivo. Requiere ADMIN.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/populares")
    @Operation(summary = "Productos más vendidos", description = "Lista los productos activos ordenados por popularidad (más vendidos).")
    public ResponseEntity<List<ProductoResponseDTO>> listarPopulares() {
        return ResponseEntity.ok(productoService.listarActivosOrdenadosPorPopularidad());
    }

    @GetMapping("/{id}/historial-precios")
    @Operation(summary = "Historial de precios", description = "Obtiene el histórico de cambios de precio de un producto.")
    public ResponseEntity<List<HistorialPrecioDTO>> obtenerHistorialPrecios(@PathVariable Long id) {
        return ResponseEntity.ok(historialPrecioService.obtenerHistorialPorProducto(id));
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "Consultar stock", description = "Devuelve la cantidad disponible de un producto.")
    public ResponseEntity<Long> obtenerStock(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto.getStock());
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar productos paginado (filtro por búsqueda y categoría)",
            description = "Versión simplificada del listado con paginación y filtros por texto y categoría.")
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductosPaginados(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "8") int size,
            @Parameter(description = "Texto de búsqueda") @RequestParam(defaultValue = "") String search,
            @Parameter(description = "ID de categoría (como string)") @RequestParam(defaultValue = "") String categoria) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idProducto").descending());
        return ResponseEntity.ok(productoService.obtenerProductosPaginados(search, categoria, pageable));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}