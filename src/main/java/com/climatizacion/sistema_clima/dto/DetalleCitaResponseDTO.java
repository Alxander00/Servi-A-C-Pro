package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DetalleCitaResponseDTO {
    private Long idDetalleCita;
    private Long idCita;
    private Long idServicio;
    private String nombreServicio;
    private Long idEquipo;
    private String descripcionEquipo;
    private BigDecimal precioCobrado;
}