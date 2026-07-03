package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.NotificacionEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.NotificacionRepository;
import com.climatizacion.sistema_clima.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public NotificacionEntity crearNotificacion(UsuarioEntity usuario, String mensaje, String tipo, String enlace) {
        NotificacionEntity notificacion = NotificacionEntity.builder()
                .usuario(usuario)
                .mensaje(mensaje)
                .tipo(tipo)
                .enlace(enlace)
                .leida(false)
                .build();
        return notificacionRepository.save(notificacion);
    }

    @Override
    public List<NotificacionEntity> obtenerNoLeidas(Long idUsuario) {
        return notificacionRepository.findByUsuario_IdUsuarioAndLeidaFalseOrderByFechaCreacionDesc(idUsuario);
    }

    @Override
    public long contarNoLeidas(Long idUsuario) {
        return notificacionRepository.countByUsuario_IdUsuarioAndLeidaFalse(idUsuario);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long idNotificacion, Long idUsuario) {
        notificacionRepository.findById(idNotificacion)
                .filter(n -> n.getUsuario().getIdUsuario().equals(idUsuario))
                .ifPresent(n -> {
                    n.setLeida(true);
                    notificacionRepository.save(n);
                });
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(Long idUsuario) {
        List<NotificacionEntity> noLeidas = obtenerNoLeidas(idUsuario);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
    }
}