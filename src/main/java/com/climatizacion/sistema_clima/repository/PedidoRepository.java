package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    List<PedidoEntity> findByIdUsuario(Integer idUsuario);
    long countByEstadoIn(List<String> estados);
    long countByIdUsuarioAndEstadoIn(Integer idUsuario, List<String> estados);
}