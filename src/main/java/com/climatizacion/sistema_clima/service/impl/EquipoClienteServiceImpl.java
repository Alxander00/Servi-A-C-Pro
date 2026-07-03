package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.EquipoClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.EquipoClienteResponseDTO;
import com.climatizacion.sistema_clima.entities.EquipoClienteEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.EquipoClienteRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.EquipoClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoClienteServiceImpl implements EquipoClienteService {

    private final EquipoClienteRepository equipoClienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EquipoClienteResponseDTO> obtenerTodos() {
        return equipoClienteRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ CORREGIDO: Ahora usa el método con JOIN FETCH
    @Override
    @Transactional(readOnly = true)
    public List<EquipoClienteResponseDTO> obtenerPorCliente(Long idCliente) {
        return equipoClienteRepository.findByCliente_IdUsuarioWithFetch(idCliente).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipoClienteResponseDTO crear(EquipoClienteRequestDTO request) {
        UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
    public EquipoClienteResponseDTO actualizar(Long id, EquipoClienteRequestDTO request) {
        EquipoClienteEntity equipo = equipoClienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (!equipo.getCliente().getIdUsuario().equals(request.getIdCliente())) {
            UsuarioEntity cliente = usuarioRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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
    public void eliminar(Long id) {
        equipoClienteRepository.deleteById(id);
    }

    @Override
    public Page<EquipoClienteResponseDTO> obtenerPorClientePaginado(Long idCliente, Pageable pageable) {
        return equipoClienteRepository.findByCliente_IdUsuario(idCliente, pageable)
                .map(this::mapToResponseDTO);
    }

    private EquipoClienteResponseDTO mapToResponseDTO(EquipoClienteEntity entity) {
        return EquipoClienteResponseDTO.builder()
                .idEquipo(entity.getIdEquipo())
                .idCliente(entity.getCliente().getIdUsuario())
                .nombreCliente(entity.getCliente().getNombres() + " " + entity.getCliente().getApellidos())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .capacidadBtu(entity.getCapacidadBtu())
                .ubicacionEnCasa(entity.getUbicacionEnCasa())
                .fechaInstalacion(entity.getFechaInstalacion())
                .fechaUltimoMantenimiento(entity.getFechaUltimoMantenimiento())
                .notas(entity.getNotas())
                .build();
    }
}