package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DetalleCitaResponseDTO {
    private Integer idDetalleCita;
    private Integer idCita;
    private Integer idServicio;
    private String nombreServicio;
    private Integer idEquipo;
    private String descripcionEquipo;
    private BigDecimal precioCobrado;
}