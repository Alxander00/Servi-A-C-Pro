package com.climatizacion.sistema_clima.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResenaRequestDTO {
    @NotNull
    private Long idProducto;

    @NotNull
    private Long idUsuario;

    @NotNull
    @Min(1) @Max(5)
    private Long calificacion;

    @NotBlank
    private String comentario;
}