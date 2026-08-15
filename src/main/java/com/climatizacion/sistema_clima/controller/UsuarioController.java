package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.UsuarioDTO;
import com.climatizacion.sistema_clima.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (clientes, técnicos, administradores)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una nueva cuenta de usuario. El rol por defecto es CLIENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado",
                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email/DUI duplicado")
    })
    public ResponseEntity<UsuarioDTO> registrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO nuevoUsuario = usuarioService.registrarUsuario(usuarioDTO);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ADMIN') or #email == authentication.principal.username")
    @Operation(summary = "Obtener usuario por email", description = "Devuelve los datos de un usuario por su correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioDTO> obtenerPorEmail(
            @Parameter(description = "Email del usuario", required = true) @PathVariable String email) {
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Devuelve la lista completa de usuarios. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> usuarios = usuarioService.buscarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar usuarios activos", description = "Devuelve solo los usuarios activos. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios activos")
    public ResponseEntity<List<UsuarioDTO>> listarActivos() {
        List<UsuarioDTO> usuarios = usuarioService.listarActivos();
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.principal.idUsuario")
    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario. Solo el propio usuario o ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDTO);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.principal.idUsuario")
    @Operation(summary = "Cambiar contraseña", description = "Actualiza la contraseña de un usuario. Solo el propio usuario o ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida")
    })
    public ResponseEntity<Void> cambiarPassword(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String nuevaPassword = request.get("password");
        usuarioService.cambiarPassword(id, nuevaPassword);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cambiar estado del usuario", description = "Activa o desactiva un usuario. Solo ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> cambiarEstado(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "Activar o desactivar") @RequestParam boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/actualizar-avatar")
    @PreAuthorize("authentication.principal.idUsuario != null")
    @Operation(summary = "Actualizar avatar", description = "Sube una nueva foto de perfil para el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar actualizado"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o demasiado grande")
    })
    public ResponseEntity<UsuarioDTO> actualizarAvatar(
            @Parameter(description = "Archivo de imagen (JPG, PNG, WEBP)") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UsuarioDTO usuarioActualizado = usuarioService.actualizarAvatar(email, file);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar usuarios paginados con filtros", description = "Devuelve una página de usuarios con filtros de búsqueda y rol. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Página de usuarios")
    public ResponseEntity<Page<UsuarioDTO>> listarUsuariosPaginados(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "8") int size,
            @Parameter(description = "Texto de búsqueda (nombre o email)") @RequestParam(defaultValue = "") String search,
            @Parameter(description = "Filtrar por rol (ADMIN, TECNICO, CLIENTE)") @RequestParam(defaultValue = "") String rol) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idUsuario").descending());
        return ResponseEntity.ok(usuarioService.obtenerUsuariosPaginados(search, rol, pageable));
    }
}