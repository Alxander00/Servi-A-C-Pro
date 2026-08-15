package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.MensajeDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.ConversacionEntity;
import com.climatizacion.sistema_clima.entities.MensajeEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.ConversacionRepository;
import com.climatizacion.sistema_clima.repository.MensajeRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Chat", description = "Endpoints para mensajería entre técnicos y clientes")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    // ===== WebSocket (NO se documenta en Swagger) =====
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            log.error("❌ Principal es null, no se puede enviar mensaje");
            return;
        }

        Long conversacionId = Long.valueOf(payload.get("conversacionId").toString());
        String contenido = (String) payload.get("contenido");
        Long idRemitente = Long.valueOf(principal.getName());

        log.info("📩 Mensaje de usuario {} en conversación {}", idRemitente, conversacionId);

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

        log.debug("✅ Mensaje enviado a usuario {}", idDestinatario);

        // 3. Lógica optimizada para notificaciones
        Long idCitaNotificacion = null;

        // 🔥 PRIMERO: Usar el idCita de la conversación si existe
        if (conversacion.getIdCita() != null) {
            idCitaNotificacion = conversacion.getIdCita();
            log.debug("📌 Usando idCita de la conversación: {}", idCitaNotificacion);
        } else {
            // 🔄 FALLBACK: Buscar cita activa (PROGRAMADA o EN_PROCESO) entre cliente y técnico
            log.debug("🔍 Buscando cita activa entre cliente {} y técnico {}",
                    conversacion.getIdCliente(), conversacion.getIdTecnico());

            List<CitaEntity> citasActivas = citaRepository.findByCliente_IdUsuarioAndTecnico_IdUsuarioAndEstadoIn(
                    conversacion.getIdCliente(),
                    conversacion.getIdTecnico(),
                    List.of(EstadoCita.PROGRAMADA, EstadoCita.EN_PROCESO)
            );

            if (!citasActivas.isEmpty()) {
                idCitaNotificacion = citasActivas.get(0).getIdCita();
                log.debug("✅ Cita activa encontrada: {}", idCitaNotificacion);
            } else {
                log.warn("⚠️ No se encontró cita activa entre cliente {} y técnico {}",
                        conversacion.getIdCliente(), conversacion.getIdTecnico());
            }
        }

        // 4. Enviar notificación con el ID de la cita (si existe)
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("mensaje", "Nuevo mensaje de " + remitente.getNombres());

        if (idCitaNotificacion != null) {
            notificacion.put("idCita", idCitaNotificacion);
            log.debug("📨 Notificación con idCita: {}", idCitaNotificacion);
        } else {
            log.debug("📨 Notificación sin idCita (no hay cita asociada)");
        }

        messagingTemplate.convertAndSendToUser(
                idDestinatario.toString(),
                "/queue/notifications",
                notificacion
        );
    }

    // ===== REST Endpoints (documentados) =====

    @GetMapping("/api/conversaciones/{id}/mensajes")
    @Operation(summary = "Obtener mensajes de una conversación",
            description = "Devuelve el historial de mensajes de una conversación específica. Marca los mensajes no leídos como leídos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensajes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensajeDTO.class))),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a esta conversación"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    public List<MensajeDTO> obtenerMensajes(
            @Parameter(description = "ID de la conversación", required = true) @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        log.debug("📂 Obteniendo mensajes de conversación {} para usuario {}", id, usuario.getIdUsuario());

        ConversacionEntity conversacion = conversacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        if (!conversacion.getIdCliente().equals(usuario.getIdUsuario()) &&
                !conversacion.getIdTecnico().equals(usuario.getIdUsuario())) {
            log.warn("⚠️ Usuario {} intentó acceder a conversación {} sin permiso",
                    usuario.getIdUsuario(), id);
            throw new RuntimeException("No tienes acceso a esta conversación");
        }

        List<MensajeEntity> mensajes = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(id);

        // Marcar mensajes como leídos
        mensajes.stream()
                .filter(m -> !m.getLeido() && !m.getIdRemitente().equals(usuario.getIdUsuario()))
                .forEach(m -> m.setLeido(true));
        mensajeRepository.saveAll(mensajes);

        log.debug("✅ {} mensajes obtenidos de conversación {}", mensajes.size(), id);

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
    @Operation(summary = "Listar conversaciones del usuario autenticado",
            description = "Devuelve todas las conversaciones en las que participa el usuario (como cliente o como técnico).")
    public List<ConversacionEntity> listarConversaciones(Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        log.debug("📋 Listando conversaciones para usuario {}", usuario.getIdUsuario());
        return conversacionRepository.findConversacionesByUsuario(usuario.getIdUsuario());
    }

    @PostMapping("/api/conversaciones/iniciar")
    @Operation(summary = "Iniciar una nueva conversación",
            description = "Crea una conversación entre un cliente y un técnico para una cita específica. " +
                    "Si ya existe una conversación para esa cita, la devuelve.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversación creada o existente",
                    content = @Content(schema = @Schema(implementation = ConversacionEntity.class))),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios (idCliente, idTecnico, idCita)"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para crear esta conversación")
    })
    public ResponseEntity<ConversacionEntity> iniciarConversacion(
            @Parameter(description = "Payload con idCliente, idTecnico e idCita", required = true)
            @RequestBody Map<String, Long> payload,
            Authentication authentication) {
        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));

        Long idCliente = payload.get("idCliente");
        Long idTecnico = payload.get("idTecnico");
        Long idCita = payload.get("idCita");

        if (idCliente == null || idTecnico == null || idCita == null) {
            log.warn("⚠️ Faltan datos para iniciar conversación: idCliente={}, idTecnico={}, idCita={}",
                    idCliente, idTecnico, idCita);
            throw new RuntimeException("Faltan datos: idCliente, idTecnico e idCita son obligatorios");
        }

        if (!usuario.getIdUsuario().equals(idCliente) && !usuario.getIdUsuario().equals(idTecnico)) {
            log.warn("⚠️ Usuario {} intentó crear conversación sin permiso", usuario.getIdUsuario());
            throw new RuntimeException("No tienes permiso para crear esta conversación");
        }

        log.info("📝 Iniciando conversación: cliente={}, técnico={}, cita={}", idCliente, idTecnico, idCita);

        return conversacionRepository.findByIdClienteAndIdTecnicoAndIdCita(idCliente, idTecnico, idCita)
                .map(conversacion -> {
                    log.debug("✅ Conversación existente encontrada: {}", conversacion.getId());
                    return ResponseEntity.ok(conversacion);
                })
                .orElseGet(() -> {
                    ConversacionEntity conversacion = ConversacionEntity.builder()
                            .idCliente(idCliente)
                            .idTecnico(idTecnico)
                            .idCita(idCita)
                            .fechaCreacion(LocalDateTime.now())
                            .build();
                    ConversacionEntity guardada = conversacionRepository.save(conversacion);
                    log.info("✅ Nueva conversación creada: {}", guardada.getId());
                    return ResponseEntity.ok(guardada);
                });
    }
}