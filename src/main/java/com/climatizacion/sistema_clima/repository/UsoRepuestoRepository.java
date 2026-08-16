package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.UsoRepuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsoRepuestoRepository extends JpaRepository<UsoRepuestoEntity, Long> {
    // Para saber qué repuestos se gastaron en una cita específica
    List<UsoRepuestoEntity> findByCita_IdCita(Long idCita);
    void deleteByCita_IdCita(Long idCita);
}