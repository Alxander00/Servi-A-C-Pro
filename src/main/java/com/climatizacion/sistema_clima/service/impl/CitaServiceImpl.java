package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.CitaService;
import com.climatizacion.sistema_clima.service.ResendEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResendEmailService resendEmailService;
    private final com.climatizacion.sistema_clima.service.CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerTodas() {
        return citaRepository.findAllWithFetch().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ NUEVO MÉTODO CON PAGINACIÓN
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponseDTO> obtenerPorTecnico(Long idTecnico, Pageable pageable) {
        return citaRepository.findByTecnico_IdUsuario(idTecnico, pageable)
                .map(this::mapToResponseDTO);
    }

    // ✅ MANTENEMOS EL MÉTODO ANTERIOR POR SI ACASO (PERO YA NO SE USA DESDE EL CONTROLLER)
    // @Override
    // @Transactional(readOnly = true)
    // public List<CitaResponseDTO> obtenerPorTecnico(Long idTecnico) {
    //     return citaRepository.findByTecnico_IdUsuarioWithFetch(idTecnico).stream()
    //             .map(this::mapToResponseDTO)
    //             .collect(Collectors.toList());
    // }

    @Override
    @Transactional
    public CitaResponseDTO crear(CitaRequestDTO request) {
        UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UsuarioEntity tecnico = usuarioRepository.findById(request.getIdTecnico())
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

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

    @Override
    @Transactional
    public CitaResponseDTO actualizar(Long id, CitaRequestDTO request) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setFechaInicio(request.getFechaInicio());
        cita.setFechaFin(request.getFechaFin());
        if (request.getEstado() != null) cita.setEstado(request.getEstado());
        cita.setNotas(request.getNotas());
        return mapToResponseDTO(citaRepository.save(cita));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String estado) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(EstadoCita.valueOf(estado.toUpperCase()));
        citaRepository.save(cita);
    }

    private CitaResponseDTO mapToResponseDTO(CitaEntity entity) {
        return CitaResponseDTO.builder()
                .idCita(entity.getIdCita())
                .idCliente(entity.getCliente().getIdUsuario())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .direccionCliente(entity.getCliente().getDireccion())
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

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerPorCliente(Long idCliente) {
        return citaRepository.findByCliente_IdUsuarioWithFetch(idCliente).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long contarCitasPorClienteYEstados(Long idCliente, List<EstadoCita> estados) {
        return citaRepository.countByCliente_IdUsuarioAndEstadoIn(idCliente, estados);
    }

    @Override
    @Transactional
    public CitaResponseDTO guardarReporteTecnico(Long idCita, String estado, String notas,
                                                 List<MultipartFile> fotosAntes,
                                                 List<MultipartFile> fotosDespues,
                                                 String firmaBase64) {
        System.out.println("📝 ========== GUARDANDO REPORTE ==========");
        System.out.println("   ID Cita: " + idCita);
        System.out.println("   Estado recibido: '" + estado + "'");
        System.out.println("   Notas: " + (notas != null ? notas : "(vacío)"));
        System.out.println("   Fotos Antes: " + (fotosAntes != null ? fotosAntes.size() : 0) + " archivos");
        System.out.println("   Fotos Despues: " + (fotosDespues != null ? fotosDespues.size() : 0) + " archivos");
        System.out.println("   Firma: " + (firmaBase64 != null ? "Sí (tamaño: " + firmaBase64.length() + " caracteres)" : "No"));

        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        EstadoCita estadoEnum;
        try {
            estadoEnum = EstadoCita.valueOf(estado.toUpperCase());
            System.out.println("   ✅ Estado convertido: " + estadoEnum);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Estado inválido: " + estado);
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
                    System.out.println("   📸 Subiendo foto ANTES: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                    urlsAntes.add(cloudinaryService.subirImagen(file));
                }
                cita.setUrlsFotosAntes(String.join(",", urlsAntes));
            }

            if (fotosDespues != null && !fotosDespues.isEmpty()) {
                List<String> urlsDespues = new ArrayList<>();
                for (MultipartFile file : fotosDespues) {
                    System.out.println("   📸 Subiendo foto DESPUES: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                    urlsDespues.add(cloudinaryService.subirImagen(file));
                }
                cita.setUrlsFotosDespues(String.join(",", urlsDespues));
            }

            if (firmaBase64 != null && !firmaBase64.isEmpty()) {
                System.out.println("   ✍️ Procesando firma...");
                if (!firmaBase64.startsWith("data:image/png;base64,") &&
                        !firmaBase64.startsWith("data:image/jpeg;base64,") &&
                        !firmaBase64.startsWith("data:image/jpg;base64,")) {
                    throw new RuntimeException("Formato de firma no válido. Solo se permiten PNG o JPG.");
                }
                long sizeBytes = (firmaBase64.length() * 3) / 4;
                if (sizeBytes > 1.5 * 1024 * 1024) {
                    throw new RuntimeException("La firma es demasiado grande (máx 1.5MB). Tamaño actual: " + sizeBytes + " bytes");
                }
                System.out.println("   ✅ Firma válida, subiendo a Cloudinary...");
                String firmaUrl = cloudinaryService.subirImagenBase64(firmaBase64);
                cita.setUrlFirmaCliente(firmaUrl);
                System.out.println("   ✅ Firma subida: " + firmaUrl);
            }

            CitaEntity citaGuardada = citaRepository.save(cita);
            System.out.println("✅ Reporte guardado exitosamente");
            return mapToResponseDTO(citaGuardada);

        } catch (IOException e) {
            System.err.println("❌ Error de IO: " + e.getMessage());
            throw new RuntimeException("Error al subir evidencias a Cloudinary: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Page<CitaResponseDTO> obtenerPorClientePaginado(Long idCliente, Pageable pageable) {
        return citaRepository.findByCliente_IdUsuario(idCliente, pageable)
                .map(this::mapToResponseDTO);
    }
}