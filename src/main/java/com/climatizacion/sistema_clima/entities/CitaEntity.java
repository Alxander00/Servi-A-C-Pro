package com.climatizacion.sistema_clima.entities;

import com.climatizacion.sistema_clima.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_usuario", nullable = false)
    private UsuarioEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico", nullable = false)
    private UsuarioEntity tecnico;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCita estado;

    @Column(length = 255)
    private String notas;

    @Column(name = "urls_fotos_antes", columnDefinition = "TEXT")
    private String urlsFotosAntes;

    @Column(name = "urls_fotos_despues", columnDefinition = "TEXT")
    private String urlsFotosDespues;

    @Column(name = "url_firma_cliente", columnDefinition = "TEXT")
    private String urlFirmaCliente;

    // En CitaEntity.java
    @Column(name = "tipo_servicio", length = 50)
    private String tipoServicio;

    @Column(name = "mensaje_cliente", columnDefinition = "TEXT")
    private String mensajeCliente;

    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsoRepuestoEntity> repuestosUtilizados = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoCita.PROGRAMADA;
        }
    }
}