package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class HistorialPrecioDTO {
    private Long id;
    private Long idProducto;
    private BigDecimal precio;
    private LocalDateTime fechaCambio;
}