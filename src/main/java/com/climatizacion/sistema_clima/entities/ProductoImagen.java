package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto_imagenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private ProductoEntity producto;

    @Column(name = "imagen_url", nullable = false)
    private String imagenUrl;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;
}