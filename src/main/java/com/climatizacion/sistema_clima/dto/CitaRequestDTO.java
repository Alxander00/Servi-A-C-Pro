package com.climatizacion.sistema_clima.dto;

import com.climatizacion.sistema_clima.enums.EstadoCita;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaRequestDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    private Long idPedido;

    @NotNull(message = "El ID del técnico es obligatorio")
    private Long idTecnico;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    private EstadoCita estado;
    private String notas;
}