package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
    Optional<ClienteEntity> findByEmail(String email);

    Optional<ClienteEntity> findByDui(String dui);

    List<ClienteEntity> findByActivoTrue();

    boolean existsByEmail(String email);
    boolean existsByDui(String dui);
}
