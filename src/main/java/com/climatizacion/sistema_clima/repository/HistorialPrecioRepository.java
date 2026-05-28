package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.HistorialPrecioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecioEntity, Long> {
    List<HistorialPrecioEntity> findByIdProductoOrderByFechaCambioAsc(Long idProducto);
}