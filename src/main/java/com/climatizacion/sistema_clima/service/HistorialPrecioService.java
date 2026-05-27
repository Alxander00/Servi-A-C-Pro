package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.HistorialPrecioDTO;

import java.math.BigDecimal;
import java.util.List;

public interface HistorialPrecioService {
    void registrarCambioPrecio(Long idProducto, BigDecimal nuevoPrecio);
    List<HistorialPrecioDTO> obtenerHistorialPorProducto(Long idProducto);
}