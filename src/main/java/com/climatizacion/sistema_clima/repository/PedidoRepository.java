package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.dto.PedidoResponseDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long>, JpaSpecificationExecutor<PedidoEntity> {

    List<PedidoEntity> findByIdUsuario(Long idUsuario);

    Page<PedidoEntity> findByIdUsuario(Long idUsuario, Pageable pageable);

    long countByEstadoIn(List<String> estados);

    long countByIdUsuarioAndEstadoIn(Long idUsuario, List<String> estados);

    @Query("SELECT new com.climatizacion.sistema_clima.dto.PedidoResponseDTO(" +
            "p.idPedido, p.idUsuario, " +
            "CONCAT(u.nombres, ' ', u.apellidos), " +
            "u.fotoUrl, p.fechaPedido, p.total, p.incluyeInstalacion, p.estado, p.direccion) " +
            "FROM PedidoEntity p " +
            "LEFT JOIN UsuarioEntity u ON p.idUsuario = u.idUsuario " +
            "WHERE p.idUsuario = :idUsuario")
    Page<PedidoResponseDTO> findPedidosByUsuarioDto(@Param("idUsuario") Long idUsuario, Pageable pageable);

    @Query("SELECT new com.climatizacion.sistema_clima.dto.PedidoResponseDTO(" +
            "p.idPedido, p.idUsuario, " +
            "CONCAT(u.nombres, ' ', u.apellidos), " +
            "u.fotoUrl, p.fechaPedido, p.total, p.incluyeInstalacion, p.estado, p.direccion) " +
            "FROM PedidoEntity p " +
            "LEFT JOIN UsuarioEntity u ON p.idUsuario = u.idUsuario " +
            "WHERE (:search IS NULL OR :search = '' OR CAST(p.idPedido AS string) LIKE CONCAT('%', :search, '%')) " +
            "AND (:estado IS NULL OR :estado = '' OR p.estado = :estado)")
    Page<PedidoResponseDTO> buscarConFiltros(@Param("search") String search,
                                             @Param("estado") String estado,
                                             Pageable pageable);
}