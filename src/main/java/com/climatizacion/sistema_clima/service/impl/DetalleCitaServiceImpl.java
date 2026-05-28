package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.DetalleCitaRequestDTO;
import com.climatizacion.sistema_clima.dto.DetalleCitaResponseDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.DetalleCitaEntity;
import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import com.climatizacion.sistema_clima.entities.ServicioEntity;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.DetalleCitaRepository;
import com.climatizacion.sistema_clima.repository.EquipoClienteRepository;
import com.climatizacion.sistema_clima.repository.ServicioRepository;
import com.climatizacion.sistema_clima.service.DetalleCitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetalleCitaServiceImpl implements DetalleCitaService {

    private final DetalleCitaRepository detalleCitaRepository;
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final EquipoClienteRepository equipoClienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleCitaResponseDTO> obtenerPorCita(Long idCita) {
        return detalleCitaRepository.findByCita_IdCita(idCita).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleCitaResponseDTO crear(DetalleCitaRequestDTO request) {
        CitaEntity cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        ServicioEntity servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        EquipoClienteEntity equipo = null;
        if (request.getIdEquipo() != null) {
            equipo = equipoClienteRepository.findById(request.getIdEquipo())
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        }

        DetalleCitaEntity detalle = DetalleCitaEntity.builder()
                .cita(cita)
                .servicio(servicio)
                .equipo(equipo)
                .precioCobrado(request.getPrecioCobrado())
                .build();

        return mapToResponseDTO(detalleCitaRepository.save(detalle));
    }

    @Override
    @Transactional
    public void eliminar(Long idDetalleCita) {
        detalleCitaRepository.deleteById(idDetalleCita);
    }

    private DetalleCitaResponseDTO mapToResponseDTO(DetalleCitaEntity entity) {
        return DetalleCitaResponseDTO.builder()
                .idDetalleCita(entity.getIdDetalleCita())
                .idCita(entity.getCita().getIdCita())
                .idServicio(entity.getServicio().getIdServicio())
                .nombreServicio(entity.getServicio().getNombre())
                .idEquipo(entity.getEquipo() != null ? entity.getEquipo().getIdEquipo() : null)
                .descripcionEquipo(entity.getEquipo() != null ? entity.getEquipo().getMarca() + " " + entity.getEquipo().getModelo() : null)
                .precioCobrado(entity.getPrecioCobrado())
                .build();
    }
}