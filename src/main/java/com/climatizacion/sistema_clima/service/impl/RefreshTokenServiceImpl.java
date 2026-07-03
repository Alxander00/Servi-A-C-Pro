package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.RefreshTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.RefreshTokenRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.security.JwtUtil;
import com.climatizacion.sistema_clima.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días en milisegundos
    private Long refreshExpirationMs;

    @Override
    @Transactional
    public RefreshTokenEntity crearRefreshToken(UsuarioEntity usuario) {
        // Revocar tokens anteriores del usuario
        revocarTodosLosTokensDeUsuario(usuario.getIdUsuario());

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshTokenEntity> validarRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void revocarRefreshToken(String token) {
        refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Override
    @Transactional
    public void revocarTodosLosTokensDeUsuario(Long idUsuario) {
        refreshTokenRepository.deleteByUsuario_IdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public String generarNuevoAccessToken(String refreshToken) {
        RefreshTokenEntity rt = validarRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido o expirado"));

        // Marcar el refresh token como usado (rotación)
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        // Generar nuevo access token
        UsuarioEntity usuario = rt.getUsuario();
        return jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());
    }
}