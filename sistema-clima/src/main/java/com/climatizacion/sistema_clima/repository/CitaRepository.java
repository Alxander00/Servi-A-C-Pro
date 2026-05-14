package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.CitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Integer> {
    List<CitaEntity> findByTecnico_IdUsuario(Long idTecnico);
    List<CitaEntity> findByCliente_IdCliente(Long idCliente);
}