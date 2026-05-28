package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoClienteRepository extends JpaRepository<EquipoClienteEntity, Long> {
    List<EquipoClienteEntity> findByCliente_IdCliente(Long idCliente);
}