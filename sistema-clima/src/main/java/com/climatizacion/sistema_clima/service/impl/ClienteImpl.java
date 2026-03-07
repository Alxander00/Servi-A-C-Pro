package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.ClienteEntity;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.service.ClienteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteEntity crearCliente(ClienteEntity cliente) {
        if (clienteRepository.existsByDui(cliente.getDui())){
            throw new RuntimeException("El DUI ya está registrado en el sistema.");
        }
        if (clienteRepository.existsByEmail(cliente.getEmail())){
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        return clienteRepository.save(cliente);
    }

    @Override
    public Optional<ClienteEntity> obtenerPorId(Long idCliente) {
        return clienteRepository.findById(idCliente);
    }

    @Override
    public List<ClienteEntity> obtenerClientesActivos() {
        return clienteRepository.findByActivoTrue();
    }

    @Override
    public ClienteEntity actualizarCliente(Long idCliente, ClienteEntity clienteActualizado) {
        return clienteRepository.findById(idCliente).map(clienteExistente -> {
            clienteExistente.setNombres(clienteActualizado.getNombres());
            clienteExistente.setApellidos(clienteActualizado.getApellidos());
            clienteExistente.setTelefono(clienteActualizado.getTelefono());
            clienteExistente.setDireccionCompleta(clienteActualizado.getDireccionCompleta());
            clienteExistente.setLatitud(clienteActualizado.getLatitud());
            clienteExistente.setLongitud(clienteActualizado.getLongitud());

            return clienteRepository.save(clienteExistente);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Override
    public void desactivarCliente(Long idCliente) {
        clienteRepository.findById(idCliente).ifPresent(cliente -> {
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        });
    }
}
