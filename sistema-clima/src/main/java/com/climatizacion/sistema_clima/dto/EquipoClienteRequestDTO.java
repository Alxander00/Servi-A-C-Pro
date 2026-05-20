package com.climatizacion.sistema_clima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipoClienteRequestDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    private String modelo;
    private Integer capacidadBtu;
    private String ubicacionEnCasa;
    private LocalDate fechaInstalacion;
    private String notas;
}