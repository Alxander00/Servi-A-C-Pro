package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {

    List<NotificacionEntity> findByUsuario_IdUsuarioAndLeidaFalseOrderByFechaCreacionDesc(Long idUsuario);

    long countByUsuario_IdUsuarioAndLeidaFalse(Long idUsuario);
}