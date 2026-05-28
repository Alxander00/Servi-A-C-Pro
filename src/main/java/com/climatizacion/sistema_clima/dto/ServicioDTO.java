package com.climatizacion.sistema_clima.dto;

import lombok.Data;

@Data
public class ServicioDTO {
    private Long idServicio;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
}