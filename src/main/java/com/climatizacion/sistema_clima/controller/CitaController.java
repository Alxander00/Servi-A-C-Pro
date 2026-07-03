package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.dto.RepuestoUsadoDTO;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import com.climatizacion.sistema_clima.security.CustomUserDetails;
import com.climatizacion.sistema_clima.service.CitaService;
import com.climatizacion.sistema_clima.service.RepuestoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final RepuestoService repuestoService;

    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    // Ahora acepta page y size
    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<Page<CitaResponseDTO>> listarPorTecnico(
            @PathVariable Long idTecnico,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.obtenerPorTecnico(idTecnico, pageable));
    }

    @PostMapping
    public ResponseEntity<CitaResponseDTO> crear(@Valid @RequestBody CitaRequestDTO request) {
        return new ResponseEntity<>(citaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CitaRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        citaService.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<CitaResponseDTO>> listarPorCliente(
            @PathVariable Long idCliente,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();
        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        if (rol.equals("CLIENTE") && !idUsuarioAutenticado.equals(idCliente)) {
            throw new RuntimeException("No tienes permiso para ver las citas de otro usuario");
        }

        return ResponseEntity.ok(citaService.obtenerPorCliente(idCliente));
    }

    @GetMapping("/conteos/pendientes/cliente/{idCliente}")
    public ResponseEntity<Long> contarCitasPendientesCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(citaService.contarCitasPorClienteYEstados(idCliente, List.of(EstadoCita.PROGRAMADA, EstadoCita.EN_PROCESO)));
    }

    @PostMapping("/{id}/reporte")
    public ResponseEntity<Void> guardarReporte(
            @PathVariable Long id,
            @RequestParam("estado") String estado,
            @RequestParam(value = "notas", required = false) String notas,
            @RequestParam(value = "fotosAntes", required = false) List<MultipartFile> fotosAntes,
            @RequestParam(value = "fotosDespues", required = false) List<MultipartFile> fotosDespues,
            @RequestParam(value = "firma", required = false) String firmaBase64,
            @RequestParam(value = "repuestos", required = false) String repuestosJson) {

        citaService.guardarReporteTecnico(id, estado, notas, fotosAntes, fotosDespues, firmaBase64);

        if (repuestosJson != null && !repuestosJson.isEmpty() && !repuestosJson.equals("[]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<RepuestoUsadoDTO> repuestosUsados = mapper.readValue(repuestosJson, new TypeReference<List<RepuestoUsadoDTO>>(){});
                repuestoService.registrarUsoYDescontarStock(id, repuestosUsados);
            } catch (Exception e) {
                throw new RuntimeException("Error al procesar los repuestos utilizados: " + e.getMessage());
            }
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}/paginado")
    public ResponseEntity<Page<CitaResponseDTO>> listarPorClientePaginado(
            @PathVariable Long idCliente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long idUsuarioAutenticado = userDetails.getIdUsuario();
        String rol = userDetails.getAuthorities().iterator().next().getAuthority();

        if (rol.equals("CLIENTE") && !idUsuarioAutenticado.equals(idCliente)) {
            throw new RuntimeException("No tienes permiso para ver las citas de otro usuario");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.obtenerPorClientePaginado(idCliente, pageable));
    }
}