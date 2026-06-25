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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
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
        return citaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerPorTecnico(Long idTecnico) {
        return citaRepository.findByTecnico_IdUsuario(idTecnico).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CitaResponseDTO crear(CitaRequestDTO request) {
        // Ahora buscamos al cliente directamente en la tabla de usuarios
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

        // ✅ NOTIFICAR AL TÉCNICO POR CORREO
        String fechaFormateada = request.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        resendEmailService.enviarCorreoNuevaCita(
                tecnico.getEmail(),
                tecnico.getNombres() + " " + tecnico.getApellidos(),
                cliente.getNombres() + " " + cliente.getApellidos(),
                fechaFormateada,
                cliente.getDireccion() // Actualizado al nuevo campo de UsuarioEntity
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
                .build();
    }

    @Override
    public List<CitaResponseDTO> obtenerPorCliente(Long idCliente) {
        // NOTA: Asegúrate de renombrar este método en tu CitaRepository a findByCliente_IdUsuario
        return citaRepository.findByCliente_IdUsuario(idCliente).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long contarCitasPorClienteYEstados(Long idCliente, List<EstadoCita> estados) {
        // NOTA: Asegúrate de renombrar este método en tu CitaRepository a countByCliente_IdUsuarioAndEstadoIn
        return citaRepository.countByCliente_IdUsuarioAndEstadoIn(idCliente, estados);
    }

    @Override
    @Transactional
    public CitaResponseDTO guardarReporte(Long idCita, String estado, String notas, List<MultipartFile> fotosAntes, List<MultipartFile> fotosDespues, String firmaBase64) {
        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(EstadoCita.valueOf(estado.toUpperCase()));
        if (notas != null) cita.setNotas(notas);

        try {
            // 1. Subir fotos de ANTES
            if (fotosAntes != null && !fotosAntes.isEmpty()) {
                List<String> urlsAntes = new java.util.ArrayList<>();
                for (MultipartFile file : fotosAntes) urlsAntes.add(cloudinaryService.subirImagen(file));
                cita.setUrlsFotosAntes(String.join(",", urlsAntes));
            }

            // 2. Subir fotos de DESPUÉS
            if (fotosDespues != null && !fotosDespues.isEmpty()) {
                List<String> urlsDespues = new java.util.ArrayList<>();
                for (MultipartFile file : fotosDespues) urlsDespues.add(cloudinaryService.subirImagen(file));
                cita.setUrlsFotosDespues(String.join(",", urlsDespues));
            }

            // 3. Subir la FIRMA (Viene en Base64)
            if (firmaBase64 != null && !firmaBase64.isEmpty()) {
                String firmaUrl = cloudinaryService.subirImagenBase64(firmaBase64);
                cita.setUrlFirmaCliente(firmaUrl);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir evidencias a Cloudinary: " + e.getMessage());
        }

        return mapToResponseDTO(citaRepository.save(cita));
    }
}