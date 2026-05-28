package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "equipos_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "capacidad_btu")
    private Long capacidadBtu;

    @Column(name = "ubicacion_en_casa", length = 100)
    private String ubicacionEnCasa;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(columnDefinition = "TEXT")
    private String notas;
}