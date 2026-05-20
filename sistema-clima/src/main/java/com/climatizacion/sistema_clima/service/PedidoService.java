package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.PedidoEntity;

import java.util.List;

public interface PedidoService {
    PedidoEntity guardar(PedidoEntity pedido);
    List<PedidoEntity> listar();
    PedidoEntity obtenerPorId(Integer id);
    void eliminar(Integer id);
}