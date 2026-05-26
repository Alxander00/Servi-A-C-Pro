package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    public ResponseEntity<?> crearPedidoCompleto(@RequestBody PedidoRequestDTO dto) {
        try {
            return new ResponseEntity<>(service.crearPedidoCompleto(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            // Ahora mandamos el error como JSON para que JS pueda leerlo
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
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

    @GetMapping("/usuario/{idUsuario}")
    public List<PedidoEntity> listarPorUsuario(@PathVariable Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        service.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }
}