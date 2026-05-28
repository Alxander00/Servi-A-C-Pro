package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.HistorialPrecioDTO;
import com.climatizacion.sistema_clima.dto.ProductoRequestDTO;
import com.climatizacion.sistema_clima.dto.ProductoResponseDTO;
import com.climatizacion.sistema_clima.service.HistorialPrecioService;
import com.climatizacion.sistema_clima.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;
    private final HistorialPrecioService historialPrecioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        ProductoResponseDTO response = productoService.crearConImagenes(dto, imagenes);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // NUEVO: PUT que acepta multipart para actualizar con imágenes
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponseDTO> actualizarConImagenes(
            @PathVariable Long id,
            @RequestPart("producto") @Valid ProductoRequestDTO dto,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        System.out.println("📥 Recibiendo actualización de producto ID: " + id);
        System.out.println("DTO: " + dto);
        ProductoResponseDTO response = productoService.actualizarConImagenes(id, dto, imagenes);
        return ResponseEntity.ok(response);
    }

    // Mantenemos el PUT original (solo JSON) por si se necesita, pero el frontend usará el de arriba
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponseDTO> actualizarJson(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
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
}