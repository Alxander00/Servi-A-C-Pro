package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Long> {
    List<CitaEntity> findByTecnico_IdUsuario(Long idTecnico);
    List<CitaEntity> findByCliente_IdUsuario(Long idCliente);
    long countByCliente_IdUsuarioAndEstadoIn(Long idCliente, List<EstadoCita> estados);

    // CitaRepository.java
    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico WHERE c.tecnico.idUsuario = :idTecnico")
    List<CitaEntity> findByTecnico_IdUsuarioWithFetch(@Param("idTecnico") Long idTecnico);

    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico WHERE c.cliente.idUsuario = :idCliente")
    List<CitaEntity> findByCliente_IdUsuarioWithFetch(@Param("idCliente") Long idCliente);

    @Query("SELECT c FROM CitaEntity c JOIN FETCH c.cliente JOIN FETCH c.tecnico")
    List<CitaEntity> findAllWithFetch();
}