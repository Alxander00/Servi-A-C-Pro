package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.entities.PasswordResetTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.repository.PasswordResetTokenRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.CloudinaryService;
import com.climatizacion.sistema_clima.service.GeocodingService;
import com.climatizacion.sistema_clima.service.ResendEmailService;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final ResendEmailService resendEmailService;
    private final GeocodingService geocodingService;
    private final CloudinaryService cloudinaryService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${resend.api.key}")
    private String resendApiKey;

    // ==================== REGISTRO ====================
    @Override
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO request) {
        System.out.println("🔍 1. Inicio registro: " + request.getEmail());
        try {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El correo electrónico '" + request.getEmail() + "' ya está registrado.");
            }
            System.out.println("✅ 2. Email válido");

            if (usuarioRepository.existsByDui(request.getDui())) {
                throw new RuntimeException("El DUI '" + request.getDui() + "' ya está registrado.");
            }
            System.out.println("✅ 3. DUI válido");

            // 4. Construir el usuario
            UsuarioEntity usuario = UsuarioEntity.builder()
                    .nombres(request.getNombre())
                    .apellidos(request.getApellido())
                    .dui(request.getDui())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fechaNacimiento(request.getFechaNacimiento())
                    .telefono(request.getTelefono())
                    .genero(request.getGenero())
                    .rol(request.getRol() != null ? request.getRol() : Rol.CLIENTE)
                    .activo(true)
                    .direccion(request.getDireccion())
                    .build();
            System.out.println("✅ 4. Usuario construido");

            // 5. Guardar el usuario
            UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);
            System.out.println("✅ 5. Usuario guardado, ID: " + usuarioGuardado.getIdUsuario());

            // 6. Geocodificar (con try-catch)
            if (usuarioGuardado.getRol() == Rol.CLIENTE
                    && usuarioGuardado.getDireccion() != null
                    && !usuarioGuardado.getDireccion().isEmpty()) {
                try {
                    System.out.println("📍 Geocodificando dirección: " + usuarioGuardado.getDireccion());
                    double[] coords = geocodingService.geocode(usuarioGuardado.getDireccion());
                    if (coords != null) {
                        usuarioGuardado.setLatitud(BigDecimal.valueOf(coords[0]));
                        usuarioGuardado.setLongitud(BigDecimal.valueOf(coords[1]));
                        usuarioRepository.save(usuarioGuardado);
                        System.out.println("✅ Coordenadas guardadas: " + coords[0] + ", " + coords[1]);
                    } else {
                        System.out.println("⚠️ No se obtuvieron coordenadas para: " + usuarioGuardado.getDireccion());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al geocodificar: " + e.getMessage());
                }
            }

            System.out.println("✅ 6. Geocodificación completada");

            UsuarioDTO dto = convertirADTO(usuarioGuardado);
            System.out.println("✅ 7. DTO convertido: " + dto);
            return dto;

        } catch (Exception e) {
            System.err.println("❌ Error en registrarUsuario: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relanzar para que el GlobalExceptionHandler lo maneje
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long idUsuario) {
        return convertirADTO(obtenerEntidadPorId(idUsuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
        return convertirADTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long idUsuario, UsuarioDTO request) {
        UsuarioEntity usuarioExistente = obtenerEntidadPorId(idUsuario);
        // Validar que el email no esté en otro usuario
        if (!usuarioExistente.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrónico '" + request.getEmail() + "' ya está registrado por otro usuario.");
        }
        if (!usuarioExistente.getDui().equals(request.getDui())
                && usuarioRepository.existsByDui(request.getDui())) {
            throw new RuntimeException("El DUI '" + request.getDui() + "' ya está registrado por otro usuario.");
        }

        usuarioExistente.setNombres(request.getNombre());
        usuarioExistente.setApellidos(request.getApellido());
        usuarioExistente.setDui(request.getDui());
        usuarioExistente.setEmail(request.getEmail());
        usuarioExistente.setFechaNacimiento(request.getFechaNacimiento());
        usuarioExistente.setTelefono(request.getTelefono());
        usuarioExistente.setGenero(request.getGenero());
        usuarioExistente.setRol(request.getRol());
        usuarioExistente.setDireccion(request.getDireccion());

        // Geocodificar si es cliente y cambió dirección
        if (request.getRol() == Rol.CLIENTE && request.getDireccion() != null && !request.getDireccion().isEmpty()) {
            try {
                double[] coords = geocodingService.geocode(request.getDireccion());
                if (coords != null) {
                    usuarioExistente.setLatitud(BigDecimal.valueOf(coords[0]));
                    usuarioExistente.setLongitud(BigDecimal.valueOf(coords[1]));
                }
            } catch (Exception e) {
                System.err.println("❌ Error al geocodificar en actualización: " + e.getMessage());
            }
        }

        return convertirADTO(usuarioRepository.save(usuarioExistente));
    }

    @Override
    @Transactional
    public void cambiarPassword(Long idUsuario, String nuevaPassword) {
        UsuarioEntity usuario = obtenerEntidadPorId(idUsuario);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idUsuario, boolean estado) {
        UsuarioEntity usuario = obtenerEntidadPorId(idUsuario);
        usuario.setActivo(estado);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void enviarLinkRecuperacion(String email) {
        UsuarioEntity user = usuarioRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.out.println("⚠️ Usuario no encontrado: " + email);
            return;
        }

        tokenRepository.deleteByUsuarioId(user.getIdUsuario());

        String token = UUID.randomUUID().toString();
        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                .token(token)
                .usuario(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password.html?token=" + token;
        String cuerpo = "<h2>Restablece tu contraseña</h2>" +
                "<p>Haz clic en el siguiente enlace para cambiar tu contraseña:</p>" +
                "<a href=\"" + resetLink + "\">" + resetLink + "</a>" +
                "<p>Este enlace expira en 1 hora.</p>" +
                "<p>Si no solicitaste este cambio, ignora este mensaje.</p>";

        if ("dummy_key_for_local_development".equals(resendApiKey)) {
            System.out.println("=========================================");
            System.out.println("🔗 Enlace de recuperación (modo desarrollo):");
            System.out.println(resetLink);
            System.out.println("=========================================");
        } else {
            resendEmailService.enviarCorreo(user.getEmail(), "Recuperación de contraseña - ClimaPro", cuerpo);
        }
    }

    @Override
    @Transactional
    public void restablecerPassword(String token, String nuevaPassword) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o ya usado"));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }
        UsuarioEntity user = resetToken.getUsuario();
        user.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    // ==================== AVATAR ====================
    @Override
    @Transactional
    public UsuarioDTO actualizarAvatar(String email, MultipartFile archivo) {
        // 1. Buscar usuario por email
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validar que el archivo no esté vacío
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debes seleccionar una imagen.");
        }

        // 3. Validar tamaño máximo (2MB)
        if (archivo.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede superar los 2MB.");
        }

        // 4. Validar tipo de archivo (opcional pero recomendado)
        String contentType = archivo.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            throw new RuntimeException("El archivo debe ser una imagen (JPG, PNG, WEBP, etc.)");
        }

        // 5. Subir la imagen a Cloudinary
        try {
            String urlPublica = cloudinaryService.subirImagen(archivo);
            usuario.setFotoUrl(urlPublica);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
        }

        // 6. Guardar usuario con la nueva URL
        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuario);

        // 7. Devolver el DTO actualizado
        return convertirADTO(usuarioActualizado);
    }

    private UsuarioEntity obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    private UsuarioDTO convertirADTO(UsuarioEntity usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombres())
                .apellido(usuario.getApellidos())
                .dui(usuario.getDui())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .password(usuario.getPassword())
                .direccion(usuario.getDireccion())
                .fotoUrl(usuario.getFotoUrl())
                .build();
    }
}