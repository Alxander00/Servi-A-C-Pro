package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.MensajeEntity;

import java.util.List;

public interface MensajeService {

    MensajeEntity guardarMensaje(Long conversacionId, Long idRemitente, String remitenteNombre, String contenido);

    List<MensajeEntity> obtenerMensajesPorConversacion(Long conversacionId);

    void marcarMensajesComoLeidos(Long conversacionId, Long idUsuario);
}