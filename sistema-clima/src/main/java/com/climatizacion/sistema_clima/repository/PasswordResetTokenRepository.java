package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalse(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.usuario.idUsuario = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}