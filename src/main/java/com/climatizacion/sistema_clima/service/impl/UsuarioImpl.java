package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.entities.PasswordResetTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.Genero;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.repository.PasswordResetTokenRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.EmailService;
import com.climatizacion.sistema_clima.service.GeocodingService;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClienteRepository clienteRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final GeocodingService geocodingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO request) {
        validarDatosUnicos(request.getEmail(), request.getDui(), null);

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nombres(request.getNombre())
                .apellidos(request.getApellido())
                .dui(request.getDui())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fechaNacimiento(request.getFechaNacimiento())
                .telefono(request.getTelefono())
                .genero(request.getGenero())
                .rol(request.getRol())
                .activo(true)
                .build();

        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

        if (request.getRol() == Rol.CLIENTE) {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setNombres(request.getNombre());
            cliente.setApellidos(request.getApellido());
            cliente.setDui(request.getDui());
            cliente.setEmail(request.getEmail());
            cliente.setPassword(usuario.getPassword());
            cliente.setTelefono(request.getTelefono());
            cliente.setFechaNacimiento(request.getFechaNacimiento());
            if (request.getGenero() != null) {
                cliente.setGenero(Genero.valueOf(request.getGenero()));
            }
            cliente.setDireccionCompleta(request.getDireccion() != null ? request.getDireccion() : "");
            cliente.setActivo(true);
            clienteRepository.save(cliente);

            try {
                emailService.enviarCorreoBienvenida(usuario.getEmail(), usuario.getNombres());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo enviar correo de bienvenida a " + usuario.getEmail() + " - " + e.getMessage());
            }
        }

        return convertirADTO(usuarioGuardado);
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
        validarDatosUnicos(request.getEmail(), request.getDui(), idUsuario);

        usuarioExistente.setNombres(request.getNombre());
        usuarioExistente.setApellidos(request.getApellido());
        usuarioExistente.setDui(request.getDui());
        usuarioExistente.setEmail(request.getEmail());
        usuarioExistente.setFechaNacimiento(request.getFechaNacimiento());
        usuarioExistente.setTelefono(request.getTelefono());
        usuarioExistente.setGenero(request.getGenero());
        usuarioExistente.setRol(request.getRol());
        usuarioExistente.setDireccion(request.getDireccion());

        // Si es CLIENTE, actualizar también la dirección en la tabla clientes y geocodificar
        if (request.getRol() == Rol.CLIENTE) {
            clienteRepository.findById(idUsuario).ifPresent(cliente -> {
                cliente.setDireccionCompleta(request.getDireccion());
                // Geocodificar la nueva dirección y guardar coordenadas
                double[] coords = geocodingService.geocode(request.getDireccion());
                if (coords != null) {
                    cliente.setLatitud(BigDecimal.valueOf(coords[0]));
                    cliente.setLongitud(BigDecimal.valueOf(coords[1]));
                }
                clienteRepository.save(cliente);
            });
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
        if (user == null) return;
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
        emailService.enviarCorreo(user.getEmail(), "Recuperación de contraseña - ClimaPro", cuerpo);
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

    private UsuarioEntity obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    private void validarDatosUnicos(String email, String dui, Long idUsuarioActual) {
        // implementar si es necesario
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
                .build();
    }
}