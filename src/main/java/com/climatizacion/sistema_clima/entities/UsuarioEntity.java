package com.climatizacion.sistema_clima.entities;

import com.climatizacion.sistema_clima.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String apellidos;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp =  "^[0-9]{8}-[0-9]$", message = "El DUI debe tener el formato 00000000-0")
    @Column(nullable = false, length = 10, unique = true)
    private String dui;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Column(name = "correo_electronico", unique = true, length = 120)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private LocalDate fechaNacimiento;

    @Size(max = 20)
    private String telefono;

    @Column(length = 5)
    private String genero;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    private boolean activo = true;

    @Column(name = "direccion")
    private String direccion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
