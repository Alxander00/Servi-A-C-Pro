package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<ServicioEntity, Integer> {
}