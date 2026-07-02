package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import org.springframework.web.multipart.MultipartFile;

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
    void enviarLinkRecuperacion(String email);
    void restablecerPassword(String token, String nuevaPassword);
    UsuarioDTO actualizarAvatar(String email, MultipartFile archivo);
}
