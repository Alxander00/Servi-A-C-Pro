package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<List<CitaResponseDTO>> listarPorTecnico(@PathVariable Long idTecnico) {
        return ResponseEntity.ok(citaService.obtenerPorTecnico(idTecnico));
    }

    @PostMapping
    public ResponseEntity<CitaResponseDTO> crear(@Valid @RequestBody CitaRequestDTO request) {
        return new ResponseEntity<>(citaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody CitaRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        citaService.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }
}