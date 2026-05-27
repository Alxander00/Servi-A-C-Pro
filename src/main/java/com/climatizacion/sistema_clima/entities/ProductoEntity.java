package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productos", indexes = {
        @Index(name = "idx_productos_categoria", columnList = "id_categoria"),
        @Index(name = "idx_productos_btu", columnList = "capacidad_btu")
})
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    @Column(length = 150, nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @NotNull(message = "La capacidad en BTU es obligatoria")
    @Positive(message = "La capacidad en BTU debe ser mayor a 0")
    @Column(name = "capacidad_btu", nullable = false)
    private Long capacidadBtu;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private Long stock = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false, foreignKey = @ForeignKey(name = "fk_productos_categoria"))
    private CategoriaEntity categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoImagen> imagenes = new ArrayList<>();
}