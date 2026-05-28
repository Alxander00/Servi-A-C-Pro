package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.ServicioEntity;
import com.climatizacion.sistema_clima.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService service;

    @PostMapping
    public ServicioEntity guardar(@RequestBody ServicioEntity servicio) {
        return service.guardar(servicio);
    }

    @GetMapping
    public List<ServicioEntity> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ServicioEntity obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}