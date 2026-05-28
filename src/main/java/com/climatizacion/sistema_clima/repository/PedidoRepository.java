package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByIdUsuario(Long idUsuario);
    long countByEstadoIn(List<String> estados);
    long countByIdUsuarioAndEstadoIn(Long idUsuario, List<String> estados);
}