package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.ConversacionEntity;
import com.climatizacion.sistema_clima.entities.MensajeEntity;
import com.climatizacion.sistema_clima.repository.ConversacionRepository;
import com.climatizacion.sistema_clima.repository.MensajeRepository;
import com.climatizacion.sistema_clima.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;

    @Override
    @Transactional
    public MensajeEntity guardarMensaje(Long conversacionId, Long idRemitente, String remitenteNombre, String contenido) {
        ConversacionEntity conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        MensajeEntity mensaje = MensajeEntity.builder()
                .conversacion(conversacion)
                .idRemitente(idRemitente)
                .remitenteNombre(remitenteNombre)
                .contenido(contenido)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();

        return mensajeRepository.save(mensaje);
    }

    @Override
    public List<MensajeEntity> obtenerMensajesPorConversacion(Long conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(conversacionId);
    }

    @Override
    @Transactional
    public void marcarMensajesComoLeidos(Long conversacionId, Long idUsuario) {
        List<MensajeEntity> mensajes = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(conversacionId);
        mensajes.stream()
                .filter(m -> !m.getLeido() && !m.getIdRemitente().equals(idUsuario))
                .forEach(m -> m.setLeido(true));
        mensajeRepository.saveAll(mensajes);
    }
}