package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.exceptions.CitaNotFoundException;
import com.climatizacion.sistema_clima.exceptions.UsuarioNoEncontradoException;
import com.climatizacion.sistema_clima.repository.*;
import com.climatizacion.sistema_clima.service.CitaService;
import com.climatizacion.sistema_clima.service.CloudinaryService;
import com.climatizacion.sistema_clima.service.ResendEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de citas técnicas.
 */
@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResendEmailService resendEmailService;
    private final CloudinaryService cloudinaryService;
    private final DetalleCitaRepository detalleCitaRepository;
    private final UsoRepuestoRepository usoRepuestoRepository;

    /**
     * Obtiene todas las citas (solo ADMIN).
     */
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerTodas() {
        return citaRepository.findAllWithFetch().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene citas de un técnico específico con paginación.
     * @param idTecnico ID del técnico
     * @param pageable Configuración de paginación
     * @return Página de citas del técnico
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponseDTO> obtenerPorTecnico(Long idTecnico, Pageable pageable) {
        return citaRepository.findByTecnico_IdUsuario(idTecnico, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Crea una nueva cita técnica.
     * @param request Datos de la cita
     * @return DTO con los datos de la cita creada
     * @throws RuntimeException si el cliente o técnico no existen, o si las fechas son incorrectas
     */
    @Override
    @Transactional
    public CitaResponseDTO crear(CitaRequestDTO request) {
        // ✅ Validar que el cliente exista
        UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Cliente no encontrado"));

        // ✅ Validar que el técnico exista y sea TÉCNICO
        UsuarioEntity tecnico = usuarioRepository.findById(request.getIdTecnico())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Técnico no encontrado"));

        if (tecnico.getRol() != Rol.TECNICO) {
            throw new RuntimeException("El usuario seleccionado no es un técnico válido");
        }

        if (!tecnico.isActivo()) {
            throw new RuntimeException("El técnico no está activo en el sistema");
        }

        // ✅ Validar fechas
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        if (request.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede programar una cita en el pasado");
        }

        PedidoEntity pedido = null;
        if (request.getIdPedido() != null) {
            pedido = pedidoRepository.findById(request.getIdPedido())
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        }

        CitaEntity cita = CitaEntity.builder()
                .cliente(cliente)
                .pedido(pedido)
                .tecnico(tecnico)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoCita.PROGRAMADA)
                .notas(request.getNotas())
                .build();

        CitaEntity citaGuardada = citaRepository.save(cita);

        // Enviar correo al técnico
        String fechaFormateada = request.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        resendEmailService.enviarCorreoNuevaCita(
                tecnico.getEmail(),
                tecnico.getNombres() + " " + tecnico.getApellidos(),
                cliente.getNombres() + " " + cliente.getApellidos(),
                fechaFormateada,
                cliente.getDireccion()
        );

        return mapToResponseDTO(citaGuardada);
    }

    /**
     * Actualiza una cita existente.
     * @param id ID de la cita
     * @param request Nuevos datos de la cita
     * @return DTO con los datos actualizados
     */
    @Override
    @Transactional
    public CitaResponseDTO actualizar(Long id, CitaRequestDTO request) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));

        // ✅ Validar fechas
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        cita.setFechaInicio(request.getFechaInicio());
        cita.setFechaFin(request.getFechaFin());
        if (request.getEstado() != null) cita.setEstado(request.getEstado());
        cita.setNotas(request.getNotas());
        return mapToResponseDTO(citaRepository.save(cita));
    }

    /**
     * Cambia el estado de una cita.
     * @param id ID de la cita
     * @param estado Nuevo estado (PROGRAMADA, EN_PROCESO, COMPLETADA, CANCELADA)
     */
    @Override
    @Transactional
    public void cambiarEstado(Long id, String estado) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));
        cita.setEstado(EstadoCita.valueOf(estado.toUpperCase()));
        citaRepository.save(cita);
    }

    /**
     * Obtiene todas las citas de un cliente específico.
     * @param idCliente ID del cliente
     * @return Lista de citas del cliente
     */
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerPorCliente(Long idCliente) {
        return citaRepository.findByCliente_IdUsuarioWithFetch(idCliente).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta las citas de un cliente con estados específicos.
     */
    @Override
    @Transactional(readOnly = true)
    public long contarCitasPorClienteYEstados(Long idCliente, List<EstadoCita> estados) {
        return citaRepository.countByCliente_IdUsuarioAndEstadoIn(idCliente, estados);
    }

    /**
     * Guarda un reporte técnico completo con evidencias (fotos, firma, notas).
     * @param idCita ID de la cita
     * @param estado Nuevo estado de la cita
     * @param notas Notas técnicas
     * @param fotosAntes Fotos antes del servicio
     * @param fotosDespues Fotos después del servicio
     * @param firmaBase64 Firma del cliente en base64
     * @return DTO con los datos actualizados
     * @throws RuntimeException si el estado es inválido o las imágenes no se pueden subir
     */
    @Override
    @Transactional
    public CitaResponseDTO guardarReporteTecnico(Long idCita, String estado, String notas,
                                                 List<MultipartFile> fotosAntes,
                                                 List<MultipartFile> fotosDespues,
                                                 String firmaBase64) {
        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));

        EstadoCita estadoEnum;
        try {
            estadoEnum = EstadoCita.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + estado + ". Valores permitidos: " + Arrays.toString(EstadoCita.values()));
        }
        cita.setEstado(estadoEnum);

        if (notas != null && !notas.isEmpty()) {
            cita.setNotas(notas);
        }

        try {
            if (fotosAntes != null && !fotosAntes.isEmpty()) {
                List<String> urlsAntes = new ArrayList<>();
                for (MultipartFile file : fotosAntes) {
                    urlsAntes.add(cloudinaryService.subirImagen(file));
                }
                cita.setUrlsFotosAntes(String.join(",", urlsAntes));
            }

            if (fotosDespues != null && !fotosDespues.isEmpty()) {
                List<String> urlsDespues = new ArrayList<>();
                for (MultipartFile file : fotosDespues) {
                    urlsDespues.add(cloudinaryService.subirImagen(file));
                }
                cita.setUrlsFotosDespues(String.join(",", urlsDespues));
            }

            if (firmaBase64 != null && !firmaBase64.isEmpty()) {
                if (!firmaBase64.startsWith("data:image/png;base64,") &&
                        !firmaBase64.startsWith("data:image/jpeg;base64,") &&
                        !firmaBase64.startsWith("data:image/jpg;base64,")) {
                    throw new RuntimeException("Formato de firma no válido. Solo se permiten PNG o JPG.");
                }
                long sizeBytes = (firmaBase64.length() * 3) / 4;
                if (sizeBytes > 1.5 * 1024 * 1024) {
                    throw new RuntimeException("La firma es demasiado grande (máx 1.5MB)");
                }
                String firmaUrl = cloudinaryService.subirImagenBase64(firmaBase64);
                cita.setUrlFirmaCliente(firmaUrl);
            }

            CitaEntity citaGuardada = citaRepository.save(cita);
            return mapToResponseDTO(citaGuardada);

        } catch (IOException e) {
            throw new RuntimeException("Error al subir evidencias a Cloudinary: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarCita(Long id) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada con ID: " + id));
        // Soft delete: cambiamos el estado a CANCELADA en lugar de eliminar físicamente
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerPorId(Long id) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada con ID: " + id));
        return mapToResponseDTO(cita);
    }

    @Override
    @Transactional
    public void archivarCita(Long idCita) {
        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));
        if (cita.getEstado() != EstadoCita.COMPLETADA && cita.getEstado() != EstadoCita.CANCELADA) {
            throw new RuntimeException("Solo se pueden archivar citas completadas o canceladas");
        }
        cita.setArchivada(true);
        citaRepository.save(cita);
    }

    @Override
    @Transactional
    public void desarchivarCita(Long idCita) {
        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));
        cita.setArchivada(false);
        citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarArchivadasPorTecnico(Long idTecnico) {
        return citaRepository.findByTecnico_IdUsuarioAndArchivadaTrue(idTecnico)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarCitaDefinitivamente(Long idCita) {
        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada"));
        // Soft delete: la marcamos como eliminada para el técnico (ya no aparecerá en ninguna lista)
        cita.setArchivada(true); // ya la mantenemos archivada para siempre
        // Podríamos añadir otro flag, pero con archivar a true y un estado no visible basta
        // O podríamos cambiar el estado a un nuevo estado "ELIMINADA" si lo deseas.
        citaRepository.save(cita);
    }

    /**
     * Obtiene citas de un cliente con paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponseDTO> obtenerPorClientePaginado(Long idCliente, Pageable pageable) {
        return citaRepository.findByCliente_IdUsuario(idCliente, pageable)
                .map(this::mapToResponseDTO);
    }

    // ===== MÉTODOS PRIVADOS =====

    private CitaResponseDTO mapToResponseDTO(CitaEntity entity) {
        return CitaResponseDTO.builder()
                .idCita(entity.getIdCita())
                .idCliente(entity.getCliente().getIdUsuario())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .direccionCliente(entity.getCliente().getDireccion())
                .telefonoCliente(entity.getCliente().getTelefono())
                .idPedido(entity.getPedido() != null ? entity.getPedido().getIdPedido() : null)
                .idTecnico(entity.getTecnico().getIdUsuario())
                .nombreTecnico(entity.getTecnico().getNombres() + " " + entity.getTecnico().getApellidos())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado())
                .notas(entity.getNotas())
                .urlsFotosAntes(entity.getUrlsFotosAntes())
                .urlsFotosDespues(entity.getUrlsFotosDespues())
                .urlFirmaCliente(entity.getUrlFirmaCliente())
                .tipoServicio(entity.getTipoServicio())
                .mensajeCliente(entity.getMensajeCliente())
                .build();
    }
}