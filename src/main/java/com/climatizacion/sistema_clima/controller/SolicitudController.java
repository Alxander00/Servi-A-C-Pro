package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.security.CustomUserDetails;
import com.climatizacion.sistema_clima.service.SolicitudServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();

        // Si es CLIENTE, solo puede crear solicitudes para sí mismo
        if ("CLIENTE".equals(rol) && !idUsuarioAutenticado.equals(request.getIdCliente())) {
            throw new RuntimeException("No puedes crear una solicitud para otro usuario");
        }

        return new ResponseEntity<>(solicitudService.crearSolicitud(request), HttpStatus.CREATED);
    }

    // 🔒 MEJORADO: Solo ADMIN puede listar solicitudes pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPendientes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(rol)) {
            throw new RuntimeException("Solo los administradores pueden listar las solicitudes pendientes");
        }

        return ResponseEntity.ok(solicitudService.listarSolicitudesPendientes());
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorCliente(
            @PathVariable Long idCliente,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();

        // Si es CLIENTE, solo puede ver sus propias solicitudes
        if ("CLIENTE".equals(rol) && !idUsuarioAutenticado.equals(idCliente)) {
            throw new RuntimeException("No tienes permiso para ver las solicitudes de otro usuario");
        }

        return ResponseEntity.ok(solicitudService.listarPorCliente(idCliente));
    }

    // 🔒 MEJORADO: Solo ADMIN puede asignar técnico
    @PostMapping("/{id}/asignar")
    public ResponseEntity<Void> asignarTecnico(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(rol)) {
            throw new RuntimeException("Solo los administradores pueden asignar técnicos a las solicitudes");
        }

        Long idTecnico = Long.valueOf(payload.get("idTecnico").toString());
        LocalDateTime fechaInicio = LocalDateTime.parse(payload.get("fechaInicio").toString());
        LocalDateTime fechaFin = LocalDateTime.parse(payload.get("fechaFin").toString());
        solicitudService.asignarTecnico(id, idTecnico, fechaInicio, fechaFin);
        return ResponseEntity.noContent().build();
    }

    // 🔒 MEJORADO: Solo ADMIN puede rechazar solicitud
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazarSolicitud(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(rol)) {
            throw new RuntimeException("Solo los administradores pueden rechazar solicitudes");
        }

        solicitudService.rechazarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conteos/pendientes/cliente/{idCliente}")
    public ResponseEntity<Long> contarSolicitudesPendientesPorCliente(
            @PathVariable Long idCliente,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();

        if ("CLIENTE".equals(rol) && !idUsuarioAutenticado.equals(idCliente)) {
            throw new RuntimeException("No tienes permiso para ver el contador de otro usuario");
        }

        return ResponseEntity.ok(solicitudService.contarPendientesPorCliente(idCliente));
    }

    @GetMapping("/cliente/{idCliente}/paginado")
    public ResponseEntity<Page<SolicitudResponseDTO>> listarPorClientePaginado(
            @PathVariable Long idCliente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String rol = userDetails.getAuthorities().iterator().next().getAuthority();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();

        if ("CLIENTE".equals(rol) && !idUsuarioAutenticado.equals(idCliente)) {
            throw new RuntimeException("No tienes permiso para ver las solicitudes de otro usuario");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(solicitudService.listarPorClientePaginado(idCliente, pageable));
    }

    @GetMapping("/conteos/pendientes")
    public ResponseEntity<Long> contarPendientes() {
        // Si tu servicio no tiene este método, créalo para que retorne:
        // solicitudRepository.countByEstado("PENDIENTE");
        return ResponseEntity.ok(solicitudService.contarPendientes());
    }
}