package com.climatizacion.sistema_clima.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoRequestDTO {
    private Long idUsuario;
    private Double total;
    private Boolean incluyeInstalacion;
    private String direccion;
    private String telefono;
    private List<DetallePedidoRequestDTO> items;
}