package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_servicio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudServicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_usuario", nullable = false)
    private UsuarioEntity cliente;

    @Column(name = "tipo_servicio", nullable = false, length = 50)
    private String tipoServicio;

    @Column(name = "fecha_preferida")
    private LocalDateTime fechaPreferida;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}