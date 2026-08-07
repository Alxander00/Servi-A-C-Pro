package com.climatizacion.sistema_clima.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    // ELIMINAMOS la relación con ClienteEntity y ponemos el ID directo
    @Column(name = "id_cliente", nullable = false)
    private Long idUsuario;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(nullable = false)
    private Double total = 0.0;

    @Column(name = "incluye_instalacion", nullable = false)
    private Boolean incluyeInstalacion;

    @Column(nullable = false, length = 30)
    private String estado = "Pendiente";

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion_instalacion", length = 255)
    private String direccion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DetallePedidoEntity> detalles = new HashSet<>();
}