package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uso_repuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsoRepuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_uso")
    private Long idUso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private CitaEntity cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repuesto", nullable = false)
    private RepuestoEntity repuesto;

    @Column(name = "cantidad_utilizada", nullable = false)
    private Double cantidadUtilizada;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}