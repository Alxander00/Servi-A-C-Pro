package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.entities.RefreshTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.security.JwtUtil;
import com.climatizacion.sistema_clima.service.RefreshTokenService;
import com.climatizacion.sistema_clima.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para login, registro y recuperación de contraseña")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un token JWT y refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(example = """
                            {
                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                              "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                              "user": {
                                "idUsuario": 1,
                                "email": "cliente@example.com",
                                "rol": "CLIENTE"
                              }
                            }"""))),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas o usuario inactivo")
    })
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");

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
    @Operation(summary = "Renovar access token", description = "Usa el refresh token para obtener un nuevo access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nuevo access token generado"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido o expirado")
    })
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
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un enlace de restablecimiento al correo.")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        usuarioService.enviarLinkRecuperacion(email);
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace de recuperación."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña usando el token recibido por correo.")
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
    @Operation(summary = "Generar hash BCrypt (solo ADMIN)", description = "Devuelve el hash de una contraseña para pruebas.")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
        return ResponseEntity.ok(Map.of("password", password, "hash", hash));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta de cliente (rol por defecto CLIENTE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email/DUI duplicado")
    })
    public ResponseEntity<?> register(@RequestBody UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getRol() == null) {
            usuarioDTO.setRol(Rol.CLIENTE);
        }
        UsuarioDTO nuevo = usuarioService.registrarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}