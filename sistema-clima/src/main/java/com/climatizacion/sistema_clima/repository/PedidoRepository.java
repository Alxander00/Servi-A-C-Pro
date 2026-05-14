package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
}