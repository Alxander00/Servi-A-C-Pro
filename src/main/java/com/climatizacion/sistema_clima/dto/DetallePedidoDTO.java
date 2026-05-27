package com.climatizacion.sistema_clima.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoDTO {

    private Integer idDetalle;
    private Integer idPedido;
    private Long idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}