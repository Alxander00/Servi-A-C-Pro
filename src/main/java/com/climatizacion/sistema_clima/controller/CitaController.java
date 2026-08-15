package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.dto.RepuestoUsadoDTO;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.service.CitaService;
import com.climatizacion.sistema_clima.service.RepuestoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Gestión de citas técnicas y reportes")
public class CitaController {

    private final CitaService citaService;
    private final RepuestoService repuestoService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar todas las citas", description = "Devuelve la lista completa de citas. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de citas")
    public ResponseEntity<List<CitaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @GetMapping("/tecnico/{idTecnico}")
    @PreAuthorize("hasAuthority('ADMIN') or #idTecnico == authentication.principal.idUsuario")
    @Operation(summary = "Listar citas por técnico", description = "Devuelve una página de citas asignadas a un técnico específico.")
    @ApiResponse(responseCode = "200", description = "Página de citas del técnico")
    public ResponseEntity<Page<CitaResponseDTO>> listarPorTecnico(
            @Parameter(description = "ID del técnico", required = true) @PathVariable Long idTecnico,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.obtenerPorTecnico(idTecnico, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Crear una nueva cita", description = "Registra una cita técnica. Solo ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cita creada",
                    content = @Content(schema = @Schema(implementation = CitaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o fechas incorrectas")
    })
    public ResponseEntity<CitaResponseDTO> crear(@Valid @RequestBody CitaRequestDTO request) {
        return new ResponseEntity<>(citaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @citaServiceImpl.obtenerPorId(#id).tecnico.idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Actualizar una cita", description = "Modifica los datos de una cita existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita actualizada"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<CitaResponseDTO> actualizar(
            @Parameter(description = "ID de la cita") @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN') or @citaServiceImpl.obtenerPorId(#id).tecnico.idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Cambiar estado de la cita", description = "Actualiza el estado de una cita (PROGRAMADA, EN_PROCESO, COMPLETADA, CANCELADA).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<Void> cambiarEstado(
            @Parameter(description = "ID de la cita") @PathVariable Long id,
            @Parameter(description = "Nuevo estado", required = true) @RequestParam String estado) {
        citaService.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    @Operation(summary = "Listar citas por cliente", description = "Devuelve todas las citas de un cliente específico.")
    @ApiResponse(responseCode = "200", description = "Lista de citas del cliente")
    public ResponseEntity<List<CitaResponseDTO>> listarPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long idCliente) {
        return ResponseEntity.ok(citaService.obtenerPorCliente(idCliente));
    }

    @GetMapping("/conteos/pendientes/cliente/{idCliente}")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    @Operation(summary = "Contar citas pendientes de un cliente", description = "Devuelve la cantidad de citas en estado PROGRAMADA o EN_PROCESO de un cliente.")
    @ApiResponse(responseCode = "200", description = "Cantidad de citas pendientes")
    public ResponseEntity<Long> contarCitasPendientesCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long idCliente) {
        return ResponseEntity.ok(citaService.contarCitasPorClienteYEstados(idCliente, List.of(EstadoCita.PROGRAMADA, EstadoCita.EN_PROCESO)));
    }

    @PostMapping("/{id}/reporte")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    @Operation(summary = "Guardar reporte técnico", description = "Permite a un técnico o ADMIN guardar evidencias (fotos, firma, repuestos) de una cita.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reporte guardado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o imagen demasiado grande"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<Void> guardarReporte(
            @Parameter(description = "ID de la cita") @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la cita", required = true) @RequestParam("estado") String estado,
            @Parameter(description = "Notas técnicas") @RequestParam(value = "notas", required = false) String notas,
            @Parameter(description = "Fotos antes del servicio") @RequestParam(value = "fotosAntes", required = false) List<MultipartFile> fotosAntes,
            @Parameter(description = "Fotos después del servicio") @RequestParam(value = "fotosDespues", required = false) List<MultipartFile> fotosDespues,
            @Parameter(description = "Firma del cliente (base64)") @RequestParam(value = "firma", required = false) String firmaBase64,
            @Parameter(description = "JSON con los repuestos utilizados") @RequestParam(value = "repuestos", required = false) String repuestosJson) {

        citaService.guardarReporteTecnico(id, estado, notas, fotosAntes, fotosDespues, firmaBase64);

        if (repuestosJson != null && !repuestosJson.isEmpty() && !repuestosJson.equals("[]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<RepuestoUsadoDTO> repuestosUsados = mapper.readValue(repuestosJson, new TypeReference<List<RepuestoUsadoDTO>>() {});
                repuestoService.registrarUsoYDescontarStock(id, repuestosUsados);
            } catch (Exception e) {
                throw new RuntimeException("Error al procesar los repuestos utilizados: " + e.getMessage());
            }
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}/paginado")
    @PreAuthorize("hasAuthority('ADMIN') or #idCliente == authentication.principal.idUsuario")
    @Operation(summary = "Listar citas por cliente con paginación", description = "Devuelve una página de citas de un cliente específico.")
    @ApiResponse(responseCode = "200", description = "Página de citas del cliente")
    public ResponseEntity<Page<CitaResponseDTO>> listarPorClientePaginado(
            @Parameter(description = "ID del cliente") @PathVariable Long idCliente,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.obtenerPorClientePaginado(idCliente, pageable));
    }
}