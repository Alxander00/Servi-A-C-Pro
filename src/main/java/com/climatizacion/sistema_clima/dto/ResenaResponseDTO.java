package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResenaResponseDTO {
    private Long id;
    private Long idProducto;
    private Long idUsuario;
    private String nombreUsuario;
    private Long calificacion;
    private String comentario;
    private LocalDateTime fecha;
    private String estado;
}