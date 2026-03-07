package com.climatizacion.sistema_clima.entities;

import com.climatizacion.sistema_clima.enums.Genero;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCLiente;

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp =  "^[0-9]{8}-[0-9]$", message = "El DUI debe tener el formato 00000000-0")
    @Column(nullable = false, length = 10, unique = true)
    private String dui;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser correo válido")
    @Column(name = "correo_electronico", nullable = false, length = 120, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion_completa", nullable = false, columnDefinition = "TEXT")
    private String direccionCompleta;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private Genero genero;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
