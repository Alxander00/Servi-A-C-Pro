package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.ResenaRequestDTO;
import com.climatizacion.sistema_clima.dto.ResenaResponseDTO;
import com.climatizacion.sistema_clima.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crearResena(@Valid @RequestBody ResenaRequestDTO request) {
        return new ResponseEntity<>(resenaService.crearResena(request.getIdUsuario(), request), HttpStatus.CREATED);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ResenaResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(resenaService.listarResenasPorProducto(productoId));
    }

    @GetMapping("/producto/{productoId}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@PathVariable Long productoId) {
        return ResponseEntity.ok(resenaService.obtenerEstadisticas(productoId));
    }

    @GetMapping("/puede-resenar")
    public ResponseEntity<Boolean> puedeResenar(
            @RequestParam Long productoId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(resenaService.puedeResenar(usuarioId, productoId));
    }
}