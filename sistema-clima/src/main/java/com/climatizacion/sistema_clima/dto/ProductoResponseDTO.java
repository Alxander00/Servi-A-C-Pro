package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoResponseDTO {
    private Long idProducto;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Long capacidadBTU;
    private Long stock;
    private Boolean activo;

    private Long idCategoria;
    private String nombreCategoria;
}
