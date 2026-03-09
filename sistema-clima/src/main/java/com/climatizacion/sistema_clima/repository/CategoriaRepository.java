package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
    boolean existsByNombre(String nombre);
}
