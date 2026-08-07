package com.climatizacion.sistema_clima.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
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
}