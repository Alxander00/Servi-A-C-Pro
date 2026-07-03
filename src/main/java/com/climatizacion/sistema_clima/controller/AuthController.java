package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.entities.RefreshTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.security.JwtUtil;
import com.climatizacion.sistema_clima.service.RefreshTokenService;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");

        try {
            UsuarioDTO user = usuarioService.buscarPorEmail(email);
            if (user == null || !user.getActivo()) {
                return ResponseEntity.status(401).body(Map.of("message", "Usuario no existe o está inactivo"));
            }

            boolean matches = passwordEncoder.matches(password, user.getPassword());
            if (!matches) {
                return ResponseEntity.status(401).body(Map.of("message", "Contraseña incorrecta"));
            }

            // Obtener entidad completa para generar refresh token
            UsuarioEntity usuarioEntity = usuarioRepository.findById(user.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Generar Access Token
            String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRol().name());

            // Generar Refresh Token
            RefreshTokenEntity refreshTokenEntity = refreshTokenService.crearRefreshToken(usuarioEntity);

            user.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshTokenEntity.getToken());
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Refresh token requerido"));
        }

        try {
            String newAccessToken = refreshTokenService.generarNuevoAccessToken(refreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            usuarioService.enviarLinkRecuperacion(email);
            return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace de recuperación."));
        } catch (RuntimeException e) {
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
        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
        return ResponseEntity.ok(Map.of("password", password, "hash", hash));
    }
}