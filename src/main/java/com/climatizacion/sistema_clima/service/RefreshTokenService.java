package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.RefreshTokenEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshTokenEntity crearRefreshToken(UsuarioEntity usuario);

    Optional<RefreshTokenEntity> validarRefreshToken(String token);

    void revocarRefreshToken(String token);

    void revocarTodosLosTokensDeUsuario(Long idUsuario);

    String generarNuevoAccessToken(String refreshToken);
}