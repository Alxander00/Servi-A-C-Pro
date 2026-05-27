package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    // ========== LOGIN ==========
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");
        System.out.println("🔐 Email: " + email);
        System.out.println("🔐 Password recibida: [" + password + "]"); // ver si hay espacios
        try {
            UsuarioDTO user = usuarioService.buscarPorEmail(email);
            if (user == null || !user.getActivo()) {
                System.out.println("❌ Usuario no encontrado o inactivo");
                return ResponseEntity.status(401).body(Map.of("message", "Usuario no existe o está inactivo"));
            }
            System.out.println("✅ Hash almacenado en DTO: " + user.getPassword());
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("🔍 ¿Coinciden? " + matches);
            if (!matches) {
                return ResponseEntity.status(401).body(Map.of("message", "Contraseña incorrecta"));
            }
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    // ========== RECUPERACIÓN DE CONTRASEÑA ==========
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            usuarioService.enviarLinkRecuperacion(email);
            // Siempre devolvemos el mismo mensaje por seguridad (no revelamos si el email existe)
            return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace de recuperación."));
        } catch (RuntimeException e) {
            // También ocultamos el error real
            return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace de recuperación."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        try {
            usuarioService.restablecerPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = new BCryptPasswordEncoder().encode(password);
        return ResponseEntity.ok(Map.of("password", password, "hash", hash));
    }
}