package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.SolicitudServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicioEntity, Long> {

    @Query("SELECT s FROM SolicitudServicioEntity s JOIN FETCH s.cliente WHERE s.estado = :estado ORDER BY s.fechaCreacion ASC")
    List<SolicitudServicioEntity> findByEstadoOrderByFechaCreacionAsc(@Param("estado") String estado);

    List<SolicitudServicioEntity> findByCliente_IdUsuario(Long idCliente);
    long countByEstado(String estado);
    long countByCliente_IdUsuarioAndEstado(Long idCliente, String estado);
}