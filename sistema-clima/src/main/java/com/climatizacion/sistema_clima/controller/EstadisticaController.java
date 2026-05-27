package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.EstadisticasDTO;
import com.climatizacion.sistema_clima.service.EstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService service;

    @GetMapping("/dashboard")
    public ResponseEntity<EstadisticasDTO> obtenerDashboard() {
        return ResponseEntity.ok(service.obtenerDashboard());
    }
}