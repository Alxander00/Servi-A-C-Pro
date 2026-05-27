package com.climatizacion.sistema_clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudRequestDTO {
    @NotNull
    private Long idCliente;
    @NotBlank
    private String tipoServicio;
    private LocalDateTime fechaPreferida;
    private String mensaje;
}