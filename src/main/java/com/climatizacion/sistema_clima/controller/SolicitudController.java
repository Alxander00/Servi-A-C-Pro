package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.service.SolicitudServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ADMIN') or (#request.idCliente == authentication.principal.idUsuario and hasAuthority('CLIENTE'))")
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(@Valid @RequestBody SolicitudRequestDTO request) {
        return new ResponseEntity<>(solicitudService.crearSolicitud(request), HttpStatus.CREATED);
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPendientes() {
        return ResponseEntity.ok(solicitudService.listarSolicitudesPendientes());
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(solicitudService.listarPorCliente(idCliente));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> asignarTecnico(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        Long idTecnico = Long.valueOf(payload.get("idTecnico").toString());
        LocalDateTime fechaInicio = LocalDateTime.parse(payload.get("fechaInicio").toString());
        LocalDateTime fechaFin = LocalDateTime.parse(payload.get("fechaFin").toString());
        solicitudService.asignarTecnico(id, idTecnico, fechaInicio, fechaFin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> rechazarSolicitud(@PathVariable Long id) {
        solicitudService.rechazarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conteos/pendientes/cliente/{idCliente}")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    public ResponseEntity<Long> contarSolicitudesPendientesPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(solicitudService.contarPendientesPorCliente(idCliente));
    }

    @GetMapping("/cliente/{idCliente}/paginado")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    public ResponseEntity<Page<SolicitudResponseDTO>> listarPorClientePaginado(
            @PathVariable Long idCliente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(solicitudService.listarPorClientePaginado(idCliente, pageable));
    }

    @GetMapping("/conteos/pendientes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Long> contarPendientes() {
        return ResponseEntity.ok(solicitudService.contarPendientes());
    }
}