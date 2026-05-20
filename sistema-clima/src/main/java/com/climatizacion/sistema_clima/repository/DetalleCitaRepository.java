package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.DetalleCitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCitaRepository extends JpaRepository<DetalleCitaEntity, Integer> {
    List<DetalleCitaEntity> findByCita_IdCita(Integer idCita);
}