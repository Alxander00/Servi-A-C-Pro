package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedidoEntity, Long> {

    @Query("SELECT d FROM DetallePedidoEntity d " +
            "LEFT JOIN FETCH d.producto prod " +
            "LEFT JOIN FETCH prod.imagenes " +
            "WHERE d.pedido.idPedido = :idPedido")
    List<DetallePedidoEntity> findByPedidoIdWithProductos(@Param("idPedido") Long idPedido);
}