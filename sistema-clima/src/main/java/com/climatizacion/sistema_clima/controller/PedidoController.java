package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    public PedidoEntity guardar(@RequestBody PedidoEntity pedido) {
        return service.guardar(pedido);
    }

    @GetMapping
    public List<PedidoEntity> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PedidoEntity obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}