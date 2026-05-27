package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.HistorialPrecioDTO;
import com.climatizacion.sistema_clima.entities.HistorialPrecioEntity;
import com.climatizacion.sistema_clima.repository.HistorialPrecioRepository;
import com.climatizacion.sistema_clima.service.HistorialPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialPrecioServiceImpl implements HistorialPrecioService {

    private final HistorialPrecioRepository historialRepository;

    @Override
    @Transactional
    public void registrarCambioPrecio(Long idProducto, BigDecimal nuevoPrecio) {
        HistorialPrecioEntity registro = HistorialPrecioEntity.builder()
                .idProducto(idProducto)
                .precio(nuevoPrecio)
                .fechaCambio(LocalDateTime.now())
                .build();
        historialRepository.save(registro);
    }

    @Override
    public List<HistorialPrecioDTO> obtenerHistorialPorProducto(Long idProducto) {
        return historialRepository.findByIdProductoOrderByFechaCambioAsc(idProducto)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private HistorialPrecioDTO convertirADTO(HistorialPrecioEntity entity) {
        return HistorialPrecioDTO.builder()
                .id(entity.getId())
                .idProducto(entity.getIdProducto())
                .precio(entity.getPrecio())
                .fechaCambio(entity.getFechaCambio())
                .build();
    }
}