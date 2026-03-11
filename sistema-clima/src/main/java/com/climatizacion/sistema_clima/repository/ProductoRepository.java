package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByActivoTrue();

    List<ProductoEntity> findByCategoria(Long idCategoria);
}
