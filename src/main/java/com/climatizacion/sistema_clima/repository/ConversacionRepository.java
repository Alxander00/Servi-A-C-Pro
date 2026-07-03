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

    // Buscar conversación por cliente y técnico
    Optional<ConversacionEntity> findByIdClienteAndIdTecnico(Long idCliente, Long idTecnico);

    // Obtener todas las conversaciones de un usuario (cliente o técnico)
    @Query("SELECT c FROM ConversacionEntity c WHERE c.idCliente = :idUsuario OR c.idTecnico = :idUsuario")
    List<ConversacionEntity> findConversacionesByUsuario(@Param("idUsuario") Long idUsuario);
}