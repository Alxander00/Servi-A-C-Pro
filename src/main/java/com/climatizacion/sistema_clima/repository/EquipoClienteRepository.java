package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoClienteRepository extends JpaRepository<EquipoClienteEntity, Long> {
    List<EquipoClienteEntity> findByCliente_IdUsuario(Long idCliente);
    // EquipoClienteRepository.java
    @Query("SELECT e FROM EquipoClienteEntity e JOIN FETCH e.cliente WHERE e.cliente.idUsuario = :idCliente")
    List<EquipoClienteEntity> findByCliente_IdUsuarioWithFetch(@Param("idCliente") Long idCliente);
}