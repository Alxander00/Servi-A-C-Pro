package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByActivoTrue();

    List<ProductoEntity> findByCategoria(Long idCategoria);

    @Query("SELECT p, COALESCE(SUM(dp.cantidad), 0) as vendido " +
            "FROM ProductoEntity p LEFT JOIN DetallePedidoEntity dp ON dp.producto = p " +
            "LEFT JOIN PedidoEntity ped ON dp.pedido = ped AND ped.estado = 'Completado' " +
            "WHERE p.activo = true GROUP BY p.idProducto ORDER BY vendido DESC")
    List<Object[]> findProductosConVentas();
}
