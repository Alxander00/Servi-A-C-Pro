package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoService {

    PedidoEntity crearPedidoCompleto(PedidoRequestDTO dto);

    PedidoEntity guardar(PedidoEntity pedido);
    List<PedidoEntity> listar();
    PedidoEntity obtenerPorId(Long id);
    void eliminar(Long id);
    List<PedidoEntity> listarPorUsuario(Long idUsuario);
    Page<PedidoEntity> listarPorUsuarioPaginado(Long idUsuario, Pageable pageable);
    void cambiarEstado(Long id, String estado);

    long contarPedidosPorEstado(List<String> estados);
    long contarPedidosPorEstadoYUsuario(Long idUsuario, List<String> estados);

    List<PedidoEntity> listarConFiltros(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                        String estado, Long idCliente, String emailCliente);
}