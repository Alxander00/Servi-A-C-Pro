package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Long> {
    List<CitaEntity> findByTecnico_IdUsuario(Long idTecnico);
    List<CitaEntity> findByCliente_IdUsuario(Long idCliente);
    long countByCliente_IdUsuarioAndEstadoIn(Long idCliente, List<EstadoCita> estados);
}