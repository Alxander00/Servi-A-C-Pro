package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    Page<ProductoEntity> findByActivoTrue(Pageable pageable);
    long countByActivoTrue();

    List<ProductoEntity> findByCategoria_IdCategoria(Long idCategoria);

    @Query("SELECT p, COALESCE(SUM(dp.cantidad), 0) as vendido " +
            "FROM ProductoEntity p LEFT JOIN DetallePedidoEntity dp ON dp.producto = p " +
            "LEFT JOIN PedidoEntity ped ON dp.pedido = ped AND ped.estado = 'Completado' " +
            "WHERE p.activo = true GROUP BY p.idProducto ORDER BY vendido DESC")
    List<Object[]> findProductosConVentas();

    @Modifying
    @Query("UPDATE ProductoEntity p SET p.stock = p.stock - :cantidad WHERE p.idProducto = :id AND p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") Long cantidad);

    @Query("SELECT p FROM ProductoEntity p " +
            "LEFT JOIN FETCH p.imagenes " +
            "LEFT JOIN FETCH p.categoria " +
            "WHERE p.activo = true " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoria IS NULL OR :categoria = '' OR CAST(p.categoria.idCategoria AS string) = :categoria)")
    Page<ProductoEntity> buscarActivosConFiltros(@Param("search") String search, @Param("categoria") String categoria, Pageable pageable);
}