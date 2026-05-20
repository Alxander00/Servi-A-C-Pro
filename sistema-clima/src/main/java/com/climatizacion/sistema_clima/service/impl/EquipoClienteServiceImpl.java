package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.EquipoClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.EquipoClienteResponseDTO;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.repository.EquipoClienteRepository;
import com.climatizacion.sistema_clima.service.EquipoClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoClienteServiceImpl implements EquipoClienteService {

    private final EquipoClienteRepository equipoClienteRepository;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EquipoClienteResponseDTO> obtenerTodos() {
        return equipoClienteRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoClienteResponseDTO> obtenerPorCliente(Long idCliente) {
        return equipoClienteRepository.findByCliente_IdCliente(idCliente).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipoClienteResponseDTO crear(EquipoClienteRequestDTO request) {
        ClienteEntity cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        EquipoClienteEntity equipo = EquipoClienteEntity.builder()
                .cliente(cliente)
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .capacidadBtu(request.getCapacidadBtu())
                .ubicacionEnCasa(request.getUbicacionEnCasa())
                .fechaInstalacion(request.getFechaInstalacion())
                .notas(request.getNotas())
                .build();

        return mapToResponseDTO(equipoClienteRepository.save(equipo));
    }

    @Override
    @Transactional
    public EquipoClienteResponseDTO actualizar(Integer id, EquipoClienteRequestDTO request) {
        EquipoClienteEntity equipo = equipoClienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (!equipo.getCliente().getIdCliente().equals(request.getIdCliente())) {
            ClienteEntity cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            equipo.setCliente(cliente);
        }

        equipo.setMarca(request.getMarca());
        equipo.setModelo(request.getModelo());
        equipo.setCapacidadBtu(request.getCapacidadBtu());
        equipo.setUbicacionEnCasa(request.getUbicacionEnCasa());
        equipo.setFechaInstalacion(request.getFechaInstalacion());
        equipo.setNotas(request.getNotas());

        return mapToResponseDTO(equipoClienteRepository.save(equipo));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        equipoClienteRepository.deleteById(id);
    }

    private EquipoClienteResponseDTO mapToResponseDTO(EquipoClienteEntity entity) {
        return EquipoClienteResponseDTO.builder()
                .idEquipo(entity.getIdEquipo())
                .idCliente(entity.getCliente().getIdCliente())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .capacidadBtu(entity.getCapacidadBtu())
                .ubicacionEnCasa(entity.getUbicacionEnCasa())
                .fechaInstalacion(entity.getFechaInstalacion())
                .notas(entity.getNotas())
                .build();
    }
}