package com.climatizacion.sistema_clima.dto;

import lombok.Data;

@Data
public class DetallePedidoRequestDTO {
    private Long idProducto;
    private Integer cantidad;
    private Double precioUnitario;
}