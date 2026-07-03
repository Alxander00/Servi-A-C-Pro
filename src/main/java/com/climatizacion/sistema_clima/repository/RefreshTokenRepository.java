package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);

    void deleteByUsuario_IdUsuario(Long idUsuario);

    void deleteByToken(String token);
}