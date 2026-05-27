package com.climatizacion.sistema_clima.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoRequestDTO {
    private Integer idUsuario;
    private Double total;
    private Boolean incluyeInstalacion;
    private String direccion;
    private List<DetallePedidoRequestDTO> items;
}