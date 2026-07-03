package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipoClienteRepository extends JpaRepository<EquipoClienteEntity, Long> {

    List<EquipoClienteEntity> findByCliente_IdUsuario(Long idCliente);

    @EntityGraph(attributePaths = {"cliente"})
    Page<EquipoClienteEntity> findByCliente_IdUsuario(Long idCliente, Pageable pageable);

    @Query("SELECT e FROM EquipoClienteEntity e JOIN FETCH e.cliente WHERE e.cliente.idUsuario = :idCliente")
    List<EquipoClienteEntity> findByCliente_IdUsuarioWithFetch(@Param("idCliente") Long idCliente);
}