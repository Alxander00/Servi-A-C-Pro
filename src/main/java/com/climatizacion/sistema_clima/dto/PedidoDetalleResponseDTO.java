package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoDetalleResponseDTO {
    private Long idPedido;
    private Long idUsuario;
    private String nombreCliente;
    private String fotoUrl;
    private LocalDateTime fechaPedido;
    private Double total;
    private Boolean incluyeInstalacion;
    private String estado;
    private String direccion;
    private String telefono;
    private List<DetallePedidoResponseDTO> detalles;
}