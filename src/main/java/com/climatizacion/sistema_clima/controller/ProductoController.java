package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.HistorialPrecioDTO;
import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.service.HistorialPrecioService;
import com.climatizacion.sistema_clima.service.ProductoService;
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
public class ProductoController {

    private final ProductoService productoService;
    private final HistorialPrecioService historialPrecioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        ProductoResponseDTO response = productoService.crearConImagenes(dto, imagenes);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> actualizarConImagenes(
            @PathVariable Long id,
            @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        System.out.println("📥 Recibiendo actualización de producto ID: " + id);
        System.out.println("DTO: " + dto);
        ProductoResponseDTO response = productoService.actualizarConImagenes(id, dto, imagenes);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> actualizarJson(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @GetMapping
    public ResponseEntity<Page<ProductoResponseDTO>> listarActivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer btuMin,
            @RequestParam(required = false) Integer btuMax,
            @RequestParam(defaultValue = "idProducto") String orden,
            @RequestParam(defaultValue = "ASC") String direccion) {

        // Validación básica de dirección
        Sort.Direction dir = Sort.Direction.fromOptionalString(direccion).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, orden));

        Page<ProductoResponseDTO> pagina = productoService.listarConFiltros(
                busqueda, categoria, marca, precioMin, precioMax, btuMin, btuMax, pageable);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/populares")
    public ResponseEntity<List<ProductoResponseDTO>> listarPopulares() {
        return ResponseEntity.ok(productoService.listarActivosOrdenadosPorPopularidad());
    }

    @GetMapping("/{id}/historial-precios")
    public ResponseEntity<List<HistorialPrecioDTO>> obtenerHistorialPrecios(@PathVariable Long id) {
        return ResponseEntity.ok(historialPrecioService.obtenerHistorialPorProducto(id));
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

    @GetMapping("/{id}/stock")
    public ResponseEntity<Long> obtenerStock(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto.getStock());
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String categoria) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idProducto").descending());
        return ResponseEntity.ok(productoService.obtenerProductosPaginados(search, categoria, pageable));
    }
}