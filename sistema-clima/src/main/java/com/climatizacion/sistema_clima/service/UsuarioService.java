package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO);

    UsuarioDTO buscarPorId(Long idUsuario);
    UsuarioDTO buscarPorEmail(String email);
    List<UsuarioDTO> buscarTodos();
    List<UsuarioDTO> listarActivos();

    UsuarioDTO actualizarUsuario(Long idUsuario ,UsuarioDTO usuarioDTO);
    void cambiarPassword(Long idUsuario, String password);

    void cambiarEstado(Long idUsuario, boolean estado);
}
