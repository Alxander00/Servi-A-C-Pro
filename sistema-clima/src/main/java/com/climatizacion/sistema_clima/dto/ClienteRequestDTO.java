package com.climatizacion.sistema_clima.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "Los nombress son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}-[0-9]$", message = "El DUI debe tener el formato 00000000-0")
    private String dui;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String correoElectronico;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String telefono;
    private String direccionCompleta;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String genero;
}
