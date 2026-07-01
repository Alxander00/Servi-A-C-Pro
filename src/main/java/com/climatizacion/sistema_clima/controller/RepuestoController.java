package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.RepuestoEntity;
import com.climatizacion.sistema_clima.service.RepuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repuestos")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoService;

    @GetMapping
    public ResponseEntity<List<RepuestoEntity>> listarActivos() {
        return ResponseEntity.ok(repuestoService.listarActivos());
    }

    @PostMapping
    public ResponseEntity<RepuestoEntity> crear(@RequestBody RepuestoEntity repuesto) {
        return ResponseEntity.ok(repuestoService.crearRepuesto(repuesto));
    }
}