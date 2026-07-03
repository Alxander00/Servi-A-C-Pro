package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.SolicitudServicioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicioEntity, Long> {

    @Query("SELECT s FROM SolicitudServicioEntity s JOIN FETCH s.cliente WHERE s.estado = :estado ORDER BY s.fechaCreacion ASC")
    List<SolicitudServicioEntity> findByEstadoOrderByFechaCreacionAsc(@Param("estado") String estado);

    List<SolicitudServicioEntity> findByCliente_IdUsuario(Long idCliente);

    @EntityGraph(attributePaths = {"cliente"})
    Page<SolicitudServicioEntity> findByCliente_IdUsuario(Long idCliente, Pageable pageable);

    long countByEstado(String estado);
    long countByCliente_IdUsuarioAndEstado(Long idCliente, String estado);

    @Query("SELECT s FROM SolicitudServicioEntity s JOIN FETCH s.cliente WHERE s.estado = :estado ORDER BY s.fechaCreacion ASC")
    List<SolicitudServicioEntity> findByEstadoOrderByFechaCreacionAscWithFetch(@Param("estado") String estado);

    @Query("SELECT s FROM SolicitudServicioEntity s JOIN FETCH s.cliente WHERE s.cliente.idUsuario = :idCliente")
    List<SolicitudServicioEntity> findByCliente_IdUsuarioWithFetch(@Param("idCliente") Long idCliente);
}