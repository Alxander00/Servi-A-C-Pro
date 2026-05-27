package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.ClienteRequestDTO;
import com.climatizacion.sistema_clima.dto.ClienteResponseDTO;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.enums.Genero;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor  // <-- Para inyectar dependencias con final
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;  // <-- CORREGIDO: se inyecta el repositorio

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@RequestBody ClienteRequestDTO clienteRequestDTO) {
        ClienteEntity nuevoCliente = new ClienteEntity();
        nuevoCliente.setNombres(clienteRequestDTO.getNombres());
        nuevoCliente.setApellidos(clienteRequestDTO.getApellidos());
        nuevoCliente.setDui(clienteRequestDTO.getDui());
        nuevoCliente.setEmail(clienteRequestDTO.getCorreoElectronico());
        nuevoCliente.setPassword(clienteRequestDTO.getPassword());
        nuevoCliente.setTelefono(clienteRequestDTO.getTelefono());
        nuevoCliente.setDireccionCompleta(clienteRequestDTO.getDireccionCompleta());
        nuevoCliente.setLatitud(clienteRequestDTO.getLatitud());
        nuevoCliente.setLongitud(clienteRequestDTO.getLongitud());
        if (clienteRequestDTO.getGenero() != null) {
            nuevoCliente.setGenero(Genero.valueOf(clienteRequestDTO.getGenero()));
        }

        ClienteEntity clienteGuardado = clienteService.crearCliente(nuevoCliente);

        ClienteResponseDTO clienteResponseDTO = mapearAResponseDTO(clienteGuardado);

        return new ResponseEntity<>(clienteResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesActivos() {
        List<ClienteEntity> clientes = clienteService.obtenerClientesActivos();

        List<ClienteResponseDTO> responseDTOs = clientes.stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable("id") Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idCliente}/coordenadas")
    public ResponseEntity<Map<String, Double>> obtenerCoordenadas(@PathVariable Long idCliente) {
        ClienteEntity cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (cliente.getLatitud() != null && cliente.getLongitud() != null) {
            return ResponseEntity.ok(Map.of(
                    "lat", cliente.getLatitud().doubleValue(),
                    "lng", cliente.getLongitud().doubleValue()
            ));
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    private ClienteResponseDTO mapearAResponseDTO(ClienteEntity clienteEntity) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(clienteEntity.getIdCliente());
        dto.setNombres(clienteEntity.getNombres());
        dto.setApellidos(clienteEntity.getApellidos());
        dto.setDui(clienteEntity.getDui());
        dto.setCorreoElectronico(clienteEntity.getEmail());
        dto.setTelefono(clienteEntity.getTelefono());
        dto.setDireccionCompleta(clienteEntity.getDireccionCompleta());
        dto.setLatitud(clienteEntity.getLatitud());
        dto.setLongitud(clienteEntity.getLongitud());
        if (clienteEntity.getGenero() != null) {
            dto.setGenero(clienteEntity.getGenero().name());
        }
        return dto;
    }
}