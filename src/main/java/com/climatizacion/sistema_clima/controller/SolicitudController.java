package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.service.SolicitudServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudServicioService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(@Valid @RequestBody SolicitudRequestDTO request) {
        return new ResponseEntity<>(solicitudService.crearSolicitud(request), HttpStatus.CREATED);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPendientes() {
        return ResponseEntity.ok(solicitudService.listarSolicitudesPendientes());
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(solicitudService.listarPorCliente(idCliente));
    }

    @PostMapping("/{id}/asignar")
    public ResponseEntity<Void> asignarTecnico(@PathVariable Long id,
                                               @RequestBody Map<String, Object> payload) {
        Long idTecnico = Long.valueOf(payload.get("idTecnico").toString());
        LocalDateTime fechaInicio = LocalDateTime.parse(payload.get("fechaInicio").toString());
        LocalDateTime fechaFin = LocalDateTime.parse(payload.get("fechaFin").toString());
        solicitudService.asignarTecnico(id, idTecnico, fechaInicio, fechaFin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazarSolicitud(@PathVariable Long id) {
        solicitudService.rechazarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conteos/pendientes")
    public ResponseEntity<Long> contarSolicitudesPendientes() {
        return ResponseEntity.ok(solicitudService.contarPendientes());
    }

    @GetMapping("/conteos/pendientes/cliente/{idCliente}")
    public ResponseEntity<Long> contarSolicitudesPendientesPorCliente(@PathVariable Long idCliente) {
        // Asegúrate de que el nombre del método en solicitudService coincida con el que tengas en tu interfaz.
        return ResponseEntity.ok(solicitudService.contarPendientesPorCliente(idCliente));
    }
}