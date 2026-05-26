package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");
        System.out.println("🔐 Intento de login - Email: " + email);
        System.out.println("🔐 Contraseña recibida: " + password);
        try {
            UsuarioDTO user = usuarioService.buscarPorEmail(email);
            if (user == null || !user.getActivo()) {
                System.out.println("❌ Usuario no encontrado o inactivo");
                return ResponseEntity.status(401).body(Map.of("message", "Usuario no existe o está inactivo"));
            }
            System.out.println("✅ Usuario encontrado: " + user.getEmail());
            System.out.println("📦 Hash almacenado en DTO: " + user.getPassword());
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("🔍 ¿Coinciden? " + matches);
            if (!matches) {
                System.out.println("❌ Contraseña incorrecta");
                return ResponseEntity.status(401).body(Map.of("message", "Contraseña incorrecta"));
            }
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}