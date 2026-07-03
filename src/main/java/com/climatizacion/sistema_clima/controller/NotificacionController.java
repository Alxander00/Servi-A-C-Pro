package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.NotificacionEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.security.CustomUserDetails;
import com.climatizacion.sistema_clima.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionEntity>> obtenerNoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("🔍 ===== /no-leidas ======");
        System.out.println("📌 userDetails: " + userDetails);
        if (userDetails == null) {
            System.out.println("❌ userDetails es NULL");
            return ResponseEntity.status(401).build();
        }

        try {
            Long idUsuario = obtenerIdUsuario(userDetails);
            System.out.println("✅ ID Usuario obtenido: " + idUsuario);
            List<NotificacionEntity> notificaciones = notificacionService.obtenerNoLeidas(idUsuario);
            System.out.println("📦 Notificaciones encontradas: " + notificaciones.size());
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/contador")
    public ResponseEntity<Long> contarNoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Long idUsuario = obtenerIdUsuario(userDetails);
            return ResponseEntity.ok(notificacionService.contarNoLeidas(idUsuario));
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<Void> marcarComoLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Long idUsuario = obtenerIdUsuario(userDetails);
            notificacionService.marcarComoLeida(id, idUsuario);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    @PatchMapping("/marcar-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Long idUsuario = obtenerIdUsuario(userDetails);
            notificacionService.marcarTodasComoLeidas(idUsuario);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    private Long obtenerIdUsuario(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getIdUsuario();
        }
        // Fallback: buscar por email (inyecta UsuarioRepository)
        String email = userDetails.getUsername();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getIdUsuario();
    }
}