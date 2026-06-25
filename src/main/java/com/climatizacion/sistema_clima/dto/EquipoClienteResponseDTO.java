package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class EquipoClienteResponseDTO {
    private Long idEquipo;
    private Long idCliente;
    private String nombreCliente; // Para mostrar el nombre en el frontend
    private String marca;
    private String modelo;
    private Long capacidadBtu;
    private String ubicacionEnCasa;
    private LocalDate fechaInstalacion;
    private LocalDate fechaUltimoMantenimiento;
    private String notas;
}