package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.enums.Genero;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO request) {
        validarDatosUnicos(request.getEmail(), request.getDui(), null);

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nombres(request.getNombre())
                .apellidos(request.getApellido())
                .dui(request.getDui())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fechaNacimiento(request.getFechaNacimiento())
                .telefono(request.getTelefono())
                .genero(request.getGenero())
                .rol(request.getRol())
                .activo(true)
                .build();

        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

        // Si el rol es CLIENTE, crear también en ClienteEntity
        if (request.getRol() == Rol.CLIENTE) {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setNombres(request.getNombre());
            cliente.setApellidos(request.getApellido());
            cliente.setDui(request.getDui());
            cliente.setEmail(request.getEmail());
            cliente.setPassword(usuario.getPassword()); // misma contraseña encriptada
            cliente.setTelefono(request.getTelefono());
            cliente.setFechaNacimiento(request.getFechaNacimiento());
            if (request.getGenero() != null) {
                cliente.setGenero(Genero.valueOf(request.getGenero()));
            }
            cliente.setDireccionCompleta(request.getDireccion() != null ? request.getDireccion() : "");
            cliente.setActivo(true);
            clienteRepository.save(cliente);
        }

        return convertirADTO(usuarioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long idUsuario) {
        return convertirADTO(obtenerEntidadPorId(idUsuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
        return convertirADTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long idUsuario, UsuarioDTO request) {
        UsuarioEntity usuarioExistente = obtenerEntidadPorId(idUsuario);

        validarDatosUnicos(request.getEmail(), request.getDui(), idUsuario);

        usuarioExistente.setNombres(request.getNombre());
        usuarioExistente.setApellidos(request.getApellido());
        usuarioExistente.setDui(request.getDui());
        usuarioExistente.setEmail(request.getEmail());
        usuarioExistente.setFechaNacimiento(request.getFechaNacimiento());
        usuarioExistente.setTelefono(request.getTelefono());
        usuarioExistente.setGenero(request.getGenero());
        usuarioExistente.setRol(request.getRol());


        return convertirADTO(usuarioRepository.save(usuarioExistente));
    }

    @Override
    @Transactional
    public void cambiarPassword(Long idUsuario, String nuevaPassword) {
        UsuarioEntity usuario = obtenerEntidadPorId(idUsuario);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idUsuario, boolean estado) {
        UsuarioEntity usuario = obtenerEntidadPorId(idUsuario);
        usuario.setActivo(estado);
        usuarioRepository.save(usuario);
    }


    private UsuarioEntity obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    private void validarDatosUnicos(String email, String dui, Long idUsuarioActual) {
    }

    private UsuarioDTO convertirADTO(UsuarioEntity usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombres())
                .apellido(usuario.getApellidos())
                .dui(usuario.getDui())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .password(usuario.getPassword())   // ← ESTA LÍNEA ES OBLIGATORIA
                .build();
    }
}
