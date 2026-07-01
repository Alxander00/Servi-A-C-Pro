package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repuesto")
    private Long idRepuesto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "unidad_medida", length = 50, nullable = false)
    private String unidadMedida; // Ej: "Unidades", "Libras", "Metros"

    @Column(name = "stock_actual", nullable = false)
    private Double stockActual;

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario; // Cuánto le cuesta a la empresa

    @Column(name = "activo")
    private Boolean activo = true;
}