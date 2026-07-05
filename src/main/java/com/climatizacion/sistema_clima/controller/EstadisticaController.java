package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.EstadisticasClienteDTO;
import com.climatizacion.sistema_clima.dto.EstadisticasDTO;
import com.climatizacion.sistema_clima.service.EstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EstadisticasDTO> obtenerDashboard() {
        return ResponseEntity.ok(estadisticaService.obtenerDashboard());
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    public ResponseEntity<EstadisticasClienteDTO> obtenerEstadisticasCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(estadisticaService.obtenerEstadisticasCliente(idCliente));
    }
}