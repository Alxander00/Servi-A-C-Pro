package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.entities.*;
import com.climatizacion.sistema_clima.enums.EstadoCita;
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

    @Override
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SolicitudServicioEntity solicitud = SolicitudServicioEntity.builder()
                .cliente(cliente)
                .tipoServicio(request.getTipoServicio())
                .fechaPreferida(request.getFechaPreferida())
                .mensaje(request.getMensaje())
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();
        SolicitudServicioEntity saved = solicitudRepository.save(solicitud);

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

    // ✅ CORREGIDO: Ahora usa el método con JOIN FETCH
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaCreacionAscWithFetch("PENDIENTE")
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // ✅ CORREGIDO: Ahora usa el método con JOIN FETCH
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorCliente(Long idCliente) {
        return solicitudRepository.findByCliente_IdUsuarioWithFetch(idCliente)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void asignarTecnico(Long idSolicitud, Long idTecnico, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        SolicitudServicioEntity solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        UsuarioEntity tecnico = usuarioRepository.findById(idTecnico)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

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
                    "tecnico.html?cita=" + cita.getIdCita() // 👈 AQUÍ ESTÁ EL CAMBIO MÁGICO
            );
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo crear la notificación para el técnico ID " + idTecnico + " - " + e.getMessage());
        }

        // ===== ENVIAR CORREO ELECTRÓNICO =====
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

    @Override
    @Transactional
    public void rechazarSolicitud(Long idSolicitud) {
        SolicitudServicioEntity solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        solicitud.setEstado("RECHAZADA");
        solicitudRepository.save(solicitud);
    }

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

    @Override
    public long contarPendientes() {
        return solicitudRepository.countByEstado("PENDIENTE");
    }

    @Override
    public long contarPendientesPorCliente(Long idCliente) {
        return solicitudRepository.countByCliente_IdUsuarioAndEstado(idCliente, "PENDIENTE");
    }

    @Override
    public Page<SolicitudResponseDTO> listarPorClientePaginado(Long idCliente, Pageable pageable) {
        return solicitudRepository.findByCliente_IdUsuario(idCliente, pageable)
                .map(this::mapToResponseDTO);
    }
}