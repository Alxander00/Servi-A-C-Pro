package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.NotificacionEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;

import java.util.List;

public interface NotificacionService {

    NotificacionEntity crearNotificacion(UsuarioEntity usuario, String mensaje, String tipo, String enlace);

    List<NotificacionEntity> obtenerNoLeidas(Long idUsuario);

    long contarNoLeidas(Long idUsuario);

    void marcarComoLeida(Long idNotificacion, Long idUsuario);

    void marcarTodasComoLeidas(Long idUsuario);
}