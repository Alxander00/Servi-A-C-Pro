package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EstadisticasClienteDTO {
    private List<Map<String, Object>> gastosPorMes;
    private List<Map<String, Object>> productosMasComprados;
    private Long totalPedidos;
    private Double totalGastado;
}