package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_cita")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_cita")
    private Long idDetalleCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private CitaEntity cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private ServicioEntity servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo")
    private EquipoClienteEntity equipo;

    @Column(name = "precio_cobrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCobrado;
}