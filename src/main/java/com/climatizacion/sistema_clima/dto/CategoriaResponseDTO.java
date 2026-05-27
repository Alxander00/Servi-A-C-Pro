package com.climatizacion.sistema_clima.dto;

import lombok.Data;

@Data
public class CategoriaResponseDTO {
    private Long idCategoria;
    private String nombre;
    private Long idCategoriaPadre;
    private String nombreCategoriaPadre;
}
