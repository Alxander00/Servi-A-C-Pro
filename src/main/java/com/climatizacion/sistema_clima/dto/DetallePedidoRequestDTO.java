package com.climatizacion.sistema_clima.dto;

import lombok.Data;

@Data
public class DetallePedidoRequestDTO {
    private Long idProducto;
    private Long cantidad;
    private Double precioUnitario;
}