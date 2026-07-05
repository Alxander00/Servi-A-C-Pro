package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.entities.NotificacionEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.security.CustomUserDetails;
import com.climatizacion.sistema_clima.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionEntity>> obtenerNoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = obtenerIdUsuario(userDetails);
        List<NotificacionEntity> notificaciones = notificacionService.obtenerNoLeidas(idUsuario);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/contador")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> contarNoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = obtenerIdUsuario(userDetails);
        return ResponseEntity.ok(notificacionService.contarNoLeidas(idUsuario));
    }

    @PatchMapping("/{id}/leida")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarComoLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = obtenerIdUsuario(userDetails);
        notificacionService.marcarComoLeida(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/marcar-todas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarTodasComoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long idUsuario = obtenerIdUsuario(userDetails);
        notificacionService.marcarTodasComoLeidas(idUsuario);
        return ResponseEntity.noContent().build();
    }

    private Long obtenerIdUsuario(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getIdUsuario();
        }
        String email = userDetails.getUsername();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getIdUsuario();
    }
}