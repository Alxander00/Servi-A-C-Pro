package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;

import java.util.List;

public interface PedidoService {
    // NUEVO MÉTODO PARA CHECKOUT COMPLETO
    PedidoEntity crearPedidoCompleto(PedidoRequestDTO dto);

    PedidoEntity guardar(PedidoEntity pedido);
    List<PedidoEntity> listar();
    PedidoEntity obtenerPorId(Integer id);
    void eliminar(Integer id);
    List<PedidoEntity> listarPorUsuario(Integer idUsuario);
    void cambiarEstado(Integer id, String estado);

    long contarPedidosPorEstado(List<String> estados);
    long contarPedidosPorEstadoYUsuario(Integer idUsuario, List<String> estados);
}