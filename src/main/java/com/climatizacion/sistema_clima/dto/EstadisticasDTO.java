package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EstadisticasDTO {
    private BigDecimal ventasTotales;
    private Long totalPedidos;
    private Long totalProductos;
    private Long totalClientes;
    private List<Map<String, Object>> ventasPorMes;
    private List<Map<String, Object>> productosMasVendidos;
    private Map<String, Long> pedidosPorEstado;
}