package com.climatizacion.sistema_clima.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClienteResponseDTO {

    private Long idCliente;
    private String nombres;
    private String apellidos;
    private String dui;
    private String correoElectronico;
    private String telefono;
    private String direccionCompleta;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String genero;
}
