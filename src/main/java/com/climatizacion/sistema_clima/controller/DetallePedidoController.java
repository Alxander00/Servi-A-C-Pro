package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.DetallePedidoDTO;
import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;
import com.climatizacion.sistema_clima.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService service;

    @PostMapping
    public DetallePedidoDTO guardar(@RequestBody DetallePedidoDTO dto) {
        return service.guardar(dto);
    }

    @GetMapping
    public List<DetallePedidoDTO> listar() {
        return service.listar();
    }
}