package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_precios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPrecioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;
}