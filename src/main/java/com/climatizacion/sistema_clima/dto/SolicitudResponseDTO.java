package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudResponseDTO {
    private Long idSolicitud;
    private Long idCliente;
    private String nombreCliente;
    private String tipoServicio;
    private LocalDateTime fechaPreferida;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaCreacion;
}