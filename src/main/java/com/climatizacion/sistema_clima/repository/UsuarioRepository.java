package com.climatizacion.sistema_clima.repository;

import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByDui(String dui);

    List<UsuarioEntity> findByActivoTrue();

    @Query("SELECT u FROM UsuarioEntity u WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:rol IS NULL OR :rol = '' OR CAST(u.rol AS string) = :rol)")
    Page<UsuarioEntity> buscarConFiltros(@Param("search") String search, @Param("rol") String rol, Pageable pageable);
}