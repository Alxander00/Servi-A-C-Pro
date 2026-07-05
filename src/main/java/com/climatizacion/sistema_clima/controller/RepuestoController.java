package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.RepuestoEntity;
import com.climatizacion.sistema_clima.service.RepuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repuestos")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    public ResponseEntity<List<RepuestoEntity>> listarActivos() {
        return ResponseEntity.ok(repuestoService.listarActivos());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RepuestoEntity> crear(@RequestBody RepuestoEntity repuesto) {
        return ResponseEntity.ok(repuestoService.crearRepuesto(repuesto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RepuestoEntity> actualizarRepuesto(@PathVariable Long id, @RequestBody RepuestoEntity repuesto) {
        RepuestoEntity actualizado = repuestoService.actualizar(id, repuesto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminarRepuesto(@PathVariable Long id) {
        repuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}