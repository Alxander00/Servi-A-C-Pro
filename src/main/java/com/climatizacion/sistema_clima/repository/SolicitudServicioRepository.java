package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.SolicitudServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicioEntity, Integer> {
    List<SolicitudServicioEntity> findByEstadoOrderByFechaCreacionAsc(String estado);
    List<SolicitudServicioEntity> findByCliente_IdCliente(Long idCliente);
    long countByEstado(String estado);
}