package com.climatizacion.sistema_clima.dto;

import lombok.Data;

@Data
public class CategoriaRequestDTO {
    private String nombre;

    private Long idCategoriaPadre;
}
