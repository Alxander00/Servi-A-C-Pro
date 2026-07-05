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
import org.springframework.security.access.prepost.PreAuthorize;
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

        // ✅ Validación de entrada
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El correo electrónico es obligatorio");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        UsuarioDTO user = usuarioService.buscarPorEmail(email);
        if (user == null || !user.getActivo()) {
            throw new RuntimeException("Usuario no existe o está inactivo");
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        UsuarioEntity usuarioEntity = usuarioRepository.findById(user.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRol().name(), user.getIdUsuario());
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.crearRefreshToken(usuarioEntity);

        user.setPassword(null);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshTokenEntity.getToken());
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token requerido");
        }

        String newAccessToken = refreshTokenService.generarNuevoAccessToken(refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        usuarioService.enviarLinkRecuperacion(email);
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace de recuperación."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token de recuperación requerido");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("La nueva contraseña es obligatoria");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }

        usuarioService.restablecerPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }

    @GetMapping("/generate-hash")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
        return ResponseEntity.ok(Map.of("password", password, "hash", hash));
    }
}