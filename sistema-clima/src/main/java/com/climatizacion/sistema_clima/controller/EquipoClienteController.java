package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.EquipoClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.EquipoClienteResponseDTO;
import com.climatizacion.sistema_clima.service.EquipoClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos-cliente")
@RequiredArgsConstructor
public class EquipoClienteController {

    private final EquipoClienteService equipoClienteService;

    @GetMapping
    public ResponseEntity<List<EquipoClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(equipoClienteService.obtenerTodos());
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<EquipoClienteResponseDTO>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(equipoClienteService.obtenerPorCliente(idCliente));
    }

    @PostMapping
    public ResponseEntity<EquipoClienteResponseDTO> crear(@Valid @RequestBody EquipoClienteRequestDTO request) {
        return new ResponseEntity<>(equipoClienteService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoClienteResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody EquipoClienteRequestDTO request) {
        return ResponseEntity.ok(equipoClienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        equipoClienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}