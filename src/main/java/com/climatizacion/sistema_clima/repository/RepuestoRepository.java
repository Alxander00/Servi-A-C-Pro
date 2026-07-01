package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.RepuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepuestoRepository extends JpaRepository<RepuestoEntity, Long> {
    // Para listar solo los repuestos que el administrador no ha desactivado
    List<RepuestoEntity> findByActivoTrue();
}