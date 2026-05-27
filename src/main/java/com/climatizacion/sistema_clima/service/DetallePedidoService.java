package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.DetallePedidoDTO;
import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;

import java.util.List;

public interface DetallePedidoService {
    DetallePedidoDTO guardar(DetallePedidoDTO dto);
    List<DetallePedidoDTO> listar();
}