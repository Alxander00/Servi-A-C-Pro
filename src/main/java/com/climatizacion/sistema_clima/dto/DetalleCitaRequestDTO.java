package com.climatizacion.sistema_clima.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleCitaRequestDTO {
    @NotNull(message = "El ID de la cita es obligatorio")
    private Long idCita;

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long idServicio;

    private Long idEquipo;

    @NotNull(message = "El precio cobrado es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal precioCobrado;
}