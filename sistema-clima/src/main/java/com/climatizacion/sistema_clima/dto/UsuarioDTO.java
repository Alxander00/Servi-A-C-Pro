package com.climatizacion.sistema_clima.dto;

import com.climatizacion.sistema_clima.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dui;
    private String password;
    private LocalDate fechaNacimiento;
    private String email;
    private String telefono;
    private String genero;
    private Rol rol;
    private Boolean activo;
    private String direccion;
}
