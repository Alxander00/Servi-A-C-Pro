package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CitaRepository extends JpaRepository<CitaEntity, Long> {

    List<CitaEntity> findByTecnico_IdUsuario(Long idTecnico);

    @EntityGraph(attributePaths = {"cliente", "tecnico"})
    Page<CitaEntity> findByTecnico_IdUsuario(Long idTecnico, Pageable pageable);

    List<CitaEntity> findByCliente_IdUsuario(Long idCliente);

    @EntityGraph(attributePaths = {"cliente", "tecnico"})
    Page<CitaEntity> findByCliente_IdUsuario(Long idCliente, Pageable pageable);

    long countByCliente_IdUsuarioAndEstadoIn(Long idCliente, List<EstadoCita> estados);

    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico WHERE c.tecnico.idUsuario = :idTecnico")
    List<CitaEntity> findByTecnico_IdUsuarioWithFetch(@Param("idTecnico") Long idTecnico);

    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico WHERE c.cliente.idUsuario = :idCliente")
    List<CitaEntity> findByCliente_IdUsuarioWithFetch(@Param("idCliente") Long idCliente);

    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico")
    List<CitaEntity> findAllWithFetch();
}