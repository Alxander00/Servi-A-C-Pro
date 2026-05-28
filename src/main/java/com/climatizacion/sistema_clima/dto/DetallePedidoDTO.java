package com.climatizacion.sistema_clima.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoDTO {

    private Long idDetalle;
    private Long idPedido;
    private Long idProducto;
    private Long cantidad;
    private BigDecimal precioUnitario;
}