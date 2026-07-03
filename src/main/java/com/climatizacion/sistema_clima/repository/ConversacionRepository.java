package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ConversacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<ConversacionEntity, Long> {

    // Buscar por cliente, técnico y cita (nuevo)
    Optional<ConversacionEntity> findByIdClienteAndIdTecnicoAndIdCita(Long idCliente, Long idTecnico, Long idCita);

    // Buscar solo por cliente y técnico (se mantiene por si acaso)
    Optional<ConversacionEntity> findByIdClienteAndIdTecnico(Long idCliente, Long idTecnico);

    // Obtener todas las conversaciones de un usuario
    @Query("SELECT c FROM ConversacionEntity c WHERE c.idCliente = :idUsuario OR c.idTecnico = :idUsuario")
    List<ConversacionEntity> findConversacionesByUsuario(@Param("idUsuario") Long idUsuario);
}