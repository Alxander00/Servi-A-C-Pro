package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.MensajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<MensajeEntity, Long> {

    @Query("SELECT m FROM MensajeEntity m WHERE m.conversacion.id = :conversacionId ORDER BY m.fechaEnvio ASC")
    List<MensajeEntity> findByConversacionIdOrderByFechaEnvioAsc(@Param("conversacionId") Long conversacionId);
}