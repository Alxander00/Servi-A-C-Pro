package com.climatizacion.sistema_clima.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MensajeDTO {
    private Long id;
    private Long idRemitente;
    private String remitenteNombre;
    private Long idDestinatario;
    private String destinatarioNombre;
    private Long conversacionId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
}