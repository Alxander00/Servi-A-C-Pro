package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByDui(String dui);
    boolean existsByEmail(String email);

    List<UsuarioEntity> findByActivoTrue();
}
