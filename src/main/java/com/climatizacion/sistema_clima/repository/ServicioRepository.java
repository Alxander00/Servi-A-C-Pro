package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.entities.ServicioEntity;
import com.climatizacion.sistema_clima.entities.SolicitudServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<ServicioEntity, Long> {
}