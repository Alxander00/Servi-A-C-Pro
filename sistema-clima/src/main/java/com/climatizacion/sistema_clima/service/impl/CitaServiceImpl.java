package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

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
        ClienteEntity cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

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

        // Nota: Los triggers de la DB validarán el choque de horarios al hacer save()
        return mapToResponseDTO(citaRepository.save(cita));
    }

    @Override
    @Transactional
    public CitaResponseDTO actualizar(Integer id, CitaRequestDTO request) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Se puede añadir la misma lógica de búsqueda de entidades que en 'crear' para actualizar relaciones
        cita.setFechaInicio(request.getFechaInicio());
        cita.setFechaFin(request.getFechaFin());
        if(request.getEstado() != null) cita.setEstado(request.getEstado());
        cita.setNotas(request.getNotas());

        return mapToResponseDTO(citaRepository.save(cita));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String estado) {
        CitaEntity cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(EstadoCita.valueOf(estado.toUpperCase()));
        citaRepository.save(cita);
    }

    private CitaResponseDTO mapToResponseDTO(CitaEntity entity) {
        return CitaResponseDTO.builder()
                .idCita(entity.getIdCita())
                .idCliente(entity.getCliente().getIdCliente())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .idPedido(entity.getPedido() != null ? entity.getPedido().getIdPedido() : null)
                .idTecnico(entity.getTecnico().getIdUsuario())
                .nombreTecnico(entity.getTecnico().getNombres() + " " + entity.getTecnico().getApellidos())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado())
                .notas(entity.getNotas())
                .build();
    }
}