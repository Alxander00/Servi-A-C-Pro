package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_tecnico", nullable = false)
    private Long idTecnico;

    // Asociamos la conversación a una cita específica
    @Column(name = "id_cita")
    private Long idCita;  // puede ser null si queremos conversaciones globales

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}