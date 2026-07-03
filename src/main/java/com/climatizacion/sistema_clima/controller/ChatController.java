package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.MensajeDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.ConversacionEntity;
import com.climatizacion.sistema_clima.entities.MensajeEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.ConversacionRepository;
import com.climatizacion.sistema_clima.repository.MensajeRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;
    private final UsuarioRepository usuarioRepository;

    private final CitaRepository citaRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            System.err.println("❌ Principal es null, no se puede enviar mensaje");
            return;
        }

        Long conversacionId = Long.valueOf(payload.get("conversacionId").toString());
        String contenido = (String) payload.get("contenido");
        Long idRemitente = Long.valueOf(principal.getName());

        UsuarioEntity remitente = usuarioRepository.findById(idRemitente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ConversacionEntity conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        Long idDestinatario;
        if (conversacion.getIdCliente().equals(remitente.getIdUsuario())) {
            idDestinatario = conversacion.getIdTecnico();
        } else {
            idDestinatario = conversacion.getIdCliente();
        }

        UsuarioEntity destinatario = usuarioRepository.findById(idDestinatario)
                .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));

        // 1. Guardar mensaje en BD
        MensajeEntity mensaje = MensajeEntity.builder()
                .conversacion(conversacion)
                .idRemitente(remitente.getIdUsuario())
                .remitenteNombre(remitente.getNombres() + " " + remitente.getApellidos())
                .contenido(contenido)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();
        mensaje = mensajeRepository.save(mensaje);

        MensajeDTO mensajeDTO = MensajeDTO.builder()
                .id(mensaje.getId())
                .idRemitente(mensaje.getIdRemitente())
                .remitenteNombre(mensaje.getRemitenteNombre())
                .idDestinatario(idDestinatario)
                .destinatarioNombre(destinatario.getNombres() + " " + destinatario.getApellidos())
                .conversacionId(conversacionId)
                .contenido(mensaje.getContenido())
                .fechaEnvio(mensaje.getFechaEnvio())
                .leido(mensaje.getLeido())
                .build();

        // 2. Enviar el mensaje por WebSocket
        messagingTemplate.convertAndSendToUser(
                idDestinatario.toString(),
                "/queue/messages",
                mensajeDTO
        );

        // 👇 3. LÓGICA MÁGICA: Buscar la cita y enviar la notificación con el ID 👇
        Long idCliente = conversacion.getIdCliente();
        Long idTecnico = conversacion.getIdTecnico();
        Long idCitaNotificacion = null;

        // Buscamos qué cita comparten este cliente y este técnico
        List<CitaEntity> citasVinculadas = citaRepository.findByCliente_IdUsuario(idCliente);
        for (CitaEntity c : citasVinculadas) {
            if (c.getTecnico() != null && c.getTecnico().getIdUsuario().equals(idTecnico)) {
                idCitaNotificacion = c.getIdCita();
                // Si la cita está activa, le damos prioridad
                if ("PROGRAMADA".equals(c.getEstado()) || "EN_PROCESO".equals(c.getEstado())) {
                    break;
                }
            }
        }

        // Usamos Map<String, Object> para poder enviar el ID como número
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("mensaje", "Nuevo mensaje de " + remitente.getNombres());

        // Si encontramos la cita, adjuntamos su ID al paquete
        if (idCitaNotificacion != null) {
            notificacion.put("idCita", idCitaNotificacion);
        }

        messagingTemplate.convertAndSendToUser(
                idDestinatario.toString(),
                "/queue/notifications",
                notificacion
        );
    }

    @GetMapping("/api/conversaciones/{id}/mensajes")
    public List<MensajeDTO> obtenerMensajes(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ConversacionEntity conversacion = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        if (!conversacion.getIdCliente().equals(usuario.getIdUsuario()) &&
                !conversacion.getIdTecnico().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No tienes acceso a esta conversación");
        }

        List<MensajeEntity> mensajes = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(id);

        mensajes.stream()
                .filter(m -> !m.getLeido() && !m.getIdRemitente().equals(usuario.getIdUsuario()))
                .forEach(m -> m.setLeido(true));
        mensajeRepository.saveAll(mensajes);

        return mensajes.stream().map(m -> MensajeDTO.builder()
                .id(m.getId())
                .idRemitente(m.getIdRemitente())
                .remitenteNombre(m.getRemitenteNombre())
                .idDestinatario(usuario.getIdUsuario())
                .destinatarioNombre(usuario.getNombres() + " " + usuario.getApellidos())
                .conversacionId(id)
                .contenido(m.getContenido())
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.getLeido())
                .build()).collect(Collectors.toList());
    }

    @GetMapping("/api/conversaciones")
    public List<ConversacionEntity> listarConversaciones(Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return conversacionRepository.findConversacionesByUsuario(usuario.getIdUsuario());
    }

    @PostMapping("/api/conversaciones/iniciar")
    public ConversacionEntity iniciarConversacion(@RequestBody Map<String, Long> payload, Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Long idCliente = payload.get("idCliente");
        Long idTecnico = payload.get("idTecnico");

        if (!usuario.getIdUsuario().equals(idCliente) && !usuario.getIdUsuario().equals(idTecnico)) {
            throw new RuntimeException("No puedes crear esta conversación");
        }

        return conversacionRepository.findByIdClienteAndIdTecnico(idCliente, idTecnico)
                .orElseGet(() -> {
                    ConversacionEntity conversacion = ConversacionEntity.builder()
                            .idCliente(idCliente)
                            .idTecnico(idTecnico)
                            .fechaCreacion(LocalDateTime.now())
                            .build();
                    return conversacionRepository.save(conversacion);
                });
    }
}