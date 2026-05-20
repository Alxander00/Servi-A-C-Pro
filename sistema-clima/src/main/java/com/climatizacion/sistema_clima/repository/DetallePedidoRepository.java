package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedidoEntity, Integer> {
}