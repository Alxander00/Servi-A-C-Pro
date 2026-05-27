package com.climatizacion.sistema_clima.dto;

import com.climatizacion.sistema_clima.enums.EstadoCita;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CitaResponseDTO {
    private Integer idCita;
    private Long idCliente;
    private String nombreCliente;
    private String direccionCliente;
    private Integer idPedido;
    private Long idTecnico;
    private String nombreTecnico;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoCita estado;
    private String notas;
}