package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.ResenaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<ResenaEntity, Integer> {
    List<ResenaEntity> findByIdProductoAndEstadoOrderByFechaDesc(Long idProducto, String estado);
    Optional<ResenaEntity> findByIdProductoAndIdUsuario(Long idProducto, Long idUsuario);

    @Query("SELECT AVG(r.calificacion) FROM ResenaEntity r WHERE r.idProducto = :idProducto AND r.estado = 'APROBADO'")
    Double obtenerPromedioCalificacion(@Param("idProducto") Long idProducto);

    @Query("SELECT COUNT(r) FROM ResenaEntity r WHERE r.idProducto = :idProducto AND r.estado = 'APROBADO'")
    Long obtenerTotalResenas(@Param("idProducto") Long idProducto);
}