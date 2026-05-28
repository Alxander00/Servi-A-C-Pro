package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.DetalleCitaRequestDTO;
import com.climatizacion.sistema_clima.dto.DetalleCitaResponseDTO;
import com.climatizacion.sistema_clima.service.DetalleCitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-citas")
@RequiredArgsConstructor
public class DetalleCitaController {

    private final DetalleCitaService detalleCitaService;

    @GetMapping("/cita/{idCita}")
    public ResponseEntity<List<DetalleCitaResponseDTO>> listarPorCita(@PathVariable Long idCita) {
        return ResponseEntity.ok(detalleCitaService.obtenerPorCita(idCita));
    }

    @PostMapping
    public ResponseEntity<DetalleCitaResponseDTO> crear(@Valid @RequestBody DetalleCitaRequestDTO request) {
        return new ResponseEntity<>(detalleCitaService.crear(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        detalleCitaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}