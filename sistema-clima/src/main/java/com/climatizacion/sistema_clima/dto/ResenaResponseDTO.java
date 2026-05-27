package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResenaResponseDTO {
    private Integer id;
    private Long idProducto;
    private Long idUsuario;
    private String nombreUsuario;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fecha;
    private String estado;
}