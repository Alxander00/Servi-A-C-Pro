package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.ResenaRequestDTO;
import com.climatizacion.sistema_clima.dto.ResenaResponseDTO;
import com.climatizacion.sistema_clima.entities.ResenaEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.DetallePedidoRepository;
import com.climatizacion.sistema_clima.repository.ResenaRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.ResenaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ResenaResponseDTO crearResena(Long usuarioId, ResenaRequestDTO request) {
        // Verificar si ya reseñó
        if (resenaRepository.findByIdProductoAndIdUsuario(request.getIdProducto(), usuarioId).isPresent()) {
            throw new RuntimeException("Ya has reseñado este producto");
        }
        // Verificar si el usuario compró el producto y el pedido está completado
        boolean puede = puedeResenar(usuarioId, request.getIdProducto());
        if (!puede) {
            throw new RuntimeException("Debes haber comprado y recibido este producto para poder reseñarlo");
        }

        ResenaEntity resena = ResenaEntity.builder()
                .idProducto(request.getIdProducto())
                .idUsuario(usuarioId)
                .calificacion(request.getCalificacion())
                .comentario(request.getComentario())
                .fecha(LocalDateTime.now())
                .estado("APROBADO")
                .build();
        resena = resenaRepository.save(resena);
        return convertirAResponseDTO(resena);
    }

    @Override
    public List<ResenaResponseDTO> listarResenasPorProducto(Long productoId) {
        return resenaRepository.findByIdProductoAndEstadoOrderByFechaDesc(productoId, "APROBADO")
                .stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> obtenerEstadisticas(Long productoId) {
        Double promedio = resenaRepository.obtenerPromedioCalificacion(productoId);
        Long total = resenaRepository.obtenerTotalResenas(productoId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("promedio", promedio != null ? promedio : 0.0);
        stats.put("total", total != null ? total : 0L);
        return stats;
    }

    @Override
    public boolean puedeResenar(Long usuarioId, Long productoId) {
        String jpql = "SELECT COUNT(dp) FROM DetallePedidoEntity dp " +
                "JOIN dp.pedido ped " +
                "WHERE ped.idUsuario = :usuarioId AND ped.estado = 'Completado' " +
                "AND dp.producto.idProducto = :productoId";
        Long count = (Long) entityManager.createQuery(jpql)
                .setParameter("usuarioId", usuarioId)
                .setParameter("productoId", productoId)
                .getSingleResult();
        return count > 0;
    }

    private ResenaResponseDTO convertirAResponseDTO(ResenaEntity entity) {
        String nombreUsuario = usuarioRepository.findById(entity.getIdUsuario())
                .map(UsuarioEntity::getNombres).orElse("Usuario");
        return ResenaResponseDTO.builder()
                .id(entity.getId())
                .idProducto(entity.getIdProducto())
                .idUsuario(entity.getIdUsuario())
                .nombreUsuario(nombreUsuario)
                .calificacion(entity.getCalificacion())
                .comentario(entity.getComentario())
                .fecha(entity.getFecha())
                .estado(entity.getEstado())
                .build();
    }
}