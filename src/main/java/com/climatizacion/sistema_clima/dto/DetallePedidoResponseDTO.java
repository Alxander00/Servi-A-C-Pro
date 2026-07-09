package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DetallePedidoResponseDTO {
    private Long idDetalle;
    private Long idProducto;
    private String nombreProducto;
    private Long cantidad;
    private BigDecimal precioUnitario;
    private List<String> imagenesUrls;
    private Long capacidadBtu;
}