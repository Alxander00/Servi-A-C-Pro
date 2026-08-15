package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.entities.*;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.exceptions.SolicitudNotFoundException;
import com.climatizacion.sistema_clima.exceptions.UsuarioNoEncontradoException;
import com.climatizacion.sistema_clima.repository.*;
import com.climatizacion.sistema_clima.service.NotificacionService;
import com.climatizacion.sistema_clima.service.ResendEmailService;
import com.climatizacion.sistema_clima.service.SolicitudServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de solicitudes de servicio técnico.
 */
@Service
@RequiredArgsConstructor
public class SolicitudServicioServiceImpl implements SolicitudServicioService {

    private final SolicitudServicioRepository solicitudRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResendEmailService resendEmailService;
    private final NotificacionService notificacionService;

    @Value("${admin.email:admin@climapro.com}")
    private String adminEmail;

    /**
     * Crea una nueva solicitud de servicio para un cliente.
     * @param request Datos de la solicitud
     * @return DTO con los datos de la solicitud creada
     * @throws RuntimeException si el cliente no existe
     */
    @Override
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        SolicitudServicioEntity solicitud = SolicitudServicioEntity.builder()
                .cliente(cliente)
                .tipoServicio(request.getTipoServicio())
                .fechaPreferida(request.getFechaPreferida())
                .mensaje(request.getMensaje())
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();
        SolicitudServicioEntity saved = solicitudRepository.save(solicitud);

        // Notificar al administrador por correo
        try {
            String asunto = "Nueva solicitud de servicio de " + cliente.getNombres() + " " + cliente.getApellidos();
            String cuerpo = "<h2>Nueva solicitud de servicio</h2>" +
                    "<p><strong>Cliente:</strong> " + cliente.getNombres() + " " + cliente.getApellidos() + "</p>" +
                    "<p><strong>Tipo:</strong> " + request.getTipoServicio() + "</p>" +
                    "<p><strong>Fecha preferida:</strong> " + (request.getFechaPreferida() != null ? request.getFechaPreferida() : "No especificada") + "</p>" +
                    "<p><strong>Mensaje:</strong> " + (request.getMensaje() != null ? request.getMensaje() : "") + "</p>" +
                    "<p>Ingresa al panel de administración para asignar técnico.</p>";
            resendEmailService.enviarCorreo(adminEmail, asunto, cuerpo);
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo notificar al administrador sobre la solicitud #" + saved.getIdSolicitud() + " - " + e.getMessage());
        }

        return mapToResponseDTO(saved);
    }

    /**
     * Lista todas las solicitudes pendientes (solo ADMIN).
     * @return Lista de solicitudes pendientes
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaCreacionAscWithFetch("PENDIENTE")
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Lista todas las solicitudes de un cliente específico.
     * @param idCliente ID del cliente
     * @return Lista de solicitudes del cliente
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorCliente(Long idCliente) {
        return solicitudRepository.findByCliente_IdUsuarioWithFetch(idCliente)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Asigna un técnico a una solicitud, creando una cita automáticamente.
     * @param idSolicitud ID de la solicitud
     * @param idTecnico ID del técnico a asignar
     * @param fechaInicio Fecha de inicio de la cita
     * @param fechaFin Fecha de fin de la cita
     * @throws RuntimeException si la solicitud no existe, el técnico no es válido, o las fechas son incorrectas
     */
    @Override
    @Transactional
    public void asignarTecnico(Long idSolicitud, Long idTecnico, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Validar que la solicitud exista y esté pendiente
        SolicitudServicioEntity solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        // ✅ Validar que el técnico exista y sea TÉCNICO
        UsuarioEntity tecnico = usuarioRepository.findById(idTecnico)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Técnico no encontrado"));

        if (tecnico.getRol() != Rol.TECNICO) {
            throw new RuntimeException("El usuario seleccionado no es un técnico válido");
        }

        if (!tecnico.isActivo()) {
            throw new RuntimeException("El técnico no está activo en el sistema");
        }

        // ✅ Validar que las fechas sean correctas
        if (fechaInicio.isAfter(fechaFin)) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        if (fechaInicio.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede programar una cita en el pasado");
        }

        // Crear y guardar la cita
        CitaEntity cita = CitaEntity.builder()
                .cliente(solicitud.getCliente())
                .tecnico(tecnico)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .estado(EstadoCita.PROGRAMADA)
                .notas(solicitud.getMensaje())
                .tipoServicio(solicitud.getTipoServicio())
                .mensajeCliente(solicitud.getMensaje())
                .build();
        citaRepository.save(cita);

        // Actualizar estado de la solicitud
        solicitud.setEstado("ASIGNADA");
        solicitudRepository.save(solicitud);

        // ===== CREAR NOTIFICACIÓN EN LA BASE DE DATOS =====
        try {
            String mensajeNotificacion = "Nueva cita asignada con " +
                    solicitud.getCliente().getNombres() + " " + solicitud.getCliente().getApellidos() +
                    " a las " + fechaInicio.format(DateTimeFormatter.ofPattern("HH:mm"));

            notificacionService.crearNotificacion(
                    tecnico,
                    mensajeNotificacion,
                    "CITA_ASIGNADA",
                    "tecnico.html?cita=" + cita.getIdCita()
            );
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo crear la notificación para el técnico ID " + idTecnico + " - " + e.getMessage());
        }

        // ===== ENVIAR CORREO ELECTRÓNICO AL TÉCNICO =====
        try {
            String fechaFormateada = fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            resendEmailService.enviarCorreoNuevaCita(
                    tecnico.getEmail(),
                    tecnico.getNombres() + " " + tecnico.getApellidos(),
                    solicitud.getCliente().getNombres() + " " + solicitud.getCliente().getApellidos(),
                    fechaFormateada,
                    solicitud.getCliente().getDireccion()
            );
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo notificar al técnico sobre la cita asociada a la solicitud #" + idSolicitud + " - " + e.getMessage());
        }
    }

    /**
     * Rechaza una solicitud de servicio.
     * @param idSolicitud ID de la solicitud a rechazar
     * @throws RuntimeException si la solicitud no existe
     */
    @Override
    @Transactional
    public void rechazarSolicitud(Long idSolicitud) {
        SolicitudServicioEntity solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        solicitud.setEstado("RECHAZADA");
        solicitudRepository.save(solicitud);
    }

    /**
     * Cuenta el número total de solicitudes pendientes.
     * @return Cantidad de solicitudes en estado PENDIENTE
     */
    @Override
    @Transactional(readOnly = true)
    public long contarPendientes() {
        return solicitudRepository.countByEstado("PENDIENTE");
    }

    /**
     * Cuenta el número de solicitudes pendientes de un cliente específico.
     * @param idCliente ID del cliente
     * @return Cantidad de solicitudes pendientes del cliente
     */
    @Override
    @Transactional(readOnly = true)
    public long contarPendientesPorCliente(Long idCliente) {
        return solicitudRepository.countByCliente_IdUsuarioAndEstado(idCliente, "PENDIENTE");
    }

    /**
     * Lista las solicitudes de un cliente con paginación.
     * @param idCliente ID del cliente
     * @param pageable Configuración de paginación
     * @return Página de solicitudes del cliente
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudResponseDTO> listarPorClientePaginado(Long idCliente, Pageable pageable) {
        return solicitudRepository.findByCliente_IdUsuario(idCliente, pageable)
                .map(this::mapToResponseDTO);
    }

    // ===== MÉTODOS PRIVADOS =====

    private SolicitudResponseDTO mapToResponseDTO(SolicitudServicioEntity entity) {
        return SolicitudResponseDTO.builder()
                .idSolicitud(entity.getIdSolicitud())
                .idCliente(entity.getCliente().getIdUsuario())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .tipoServicio(entity.getTipoServicio())
                .fechaPreferida(entity.getFechaPreferida())
                .mensaje(entity.getMensaje())
                .estado(entity.getEstado())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }
}