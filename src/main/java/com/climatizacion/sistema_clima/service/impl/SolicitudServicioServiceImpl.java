package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.SolicitudRequestDTO;
import com.climatizacion.sistema_clima.dto.SolicitudResponseDTO;
import com.climatizacion.sistema_clima.entities.*;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.repository.*;
import com.climatizacion.sistema_clima.service.EmailService;
import com.climatizacion.sistema_clima.service.SolicitudServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Value("${admin.email:admin@climapro.com}")
    private String adminEmail;

    @Override
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        ClienteEntity cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        SolicitudServicioEntity solicitud = SolicitudServicioEntity.builder()
                .cliente(cliente)
                .tipoServicio(request.getTipoServicio())
                .fechaPreferida(request.getFechaPreferida())
                .mensaje(request.getMensaje())
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();
        SolicitudServicioEntity saved = solicitudRepository.save(solicitud);

        // Notificar al administrador (opcional, comentar si no hay correo real)
        String asunto = "Nueva solicitud de servicio de " + cliente.getNombres() + " " + cliente.getApellidos();
        String cuerpo = "<h2>Nueva solicitud de servicio</h2>" +
                "<p><strong>Cliente:</strong> " + cliente.getNombres() + " " + cliente.getApellidos() + "</p>" +
                "<p><strong>Tipo:</strong> " + request.getTipoServicio() + "</p>" +
                "<p><strong>Fecha preferida:</strong> " + (request.getFechaPreferida() != null ? request.getFechaPreferida() : "No especificada") + "</p>" +
                "<p><strong>Mensaje:</strong> " + (request.getMensaje() != null ? request.getMensaje() : "") + "</p>" +
                "<p>Ingresa al panel de administración para asignar técnico.</p>";
        emailService.enviarCorreo(adminEmail, asunto, cuerpo);

        return mapToResponseDTO(saved);
    }

    @Override
    public List<SolicitudResponseDTO> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaCreacionAsc("PENDIENTE")
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> listarPorCliente(Long idCliente) {
        return solicitudRepository.findByCliente_IdCliente(idCliente)
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
        CitaEntity cita = CitaEntity.builder()
                .cliente(solicitud.getCliente())
                .tecnico(tecnico)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .estado(EstadoCita.PROGRAMADA)
                .notas(solicitud.getMensaje())
                .build();
        citaRepository.save(cita);
        solicitud.setEstado("ASIGNADA");
        solicitudRepository.save(solicitud);

        // Notificar al técnico
        String fechaFormateada = fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        emailService.enviarCorreoNuevaCita(
                tecnico.getEmail(),
                tecnico.getNombres() + " " + tecnico.getApellidos(),
                solicitud.getCliente().getNombres() + " " + solicitud.getCliente().getApellidos(),
                fechaFormateada,
                solicitud.getCliente().getDireccionCompleta()
        );
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
                .idCliente(entity.getCliente().getIdCliente())
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
}