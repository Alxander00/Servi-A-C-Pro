package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long>, JpaSpecificationExecutor<PedidoEntity> {
    List<PedidoEntity> findByIdUsuario(Long idUsuario);
    long countByEstadoIn(List<String> estados);
    long countByIdUsuarioAndEstadoIn(Long idUsuario, List<String> estados);
}