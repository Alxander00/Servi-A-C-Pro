package com.climatizacion.sistema_clima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "La capacidad BTU es requerida")
    @Positive(message = "La capacidad BTU debe ser mayor a cero")
    @JsonProperty("capacidadBtu")
    private Long capacidadBTU;

    @NotNull(message = "El stock inicial es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Long stock;

    @NotNull(message = "La categoria es obligatoria")
    private Long idCategoria;
}
