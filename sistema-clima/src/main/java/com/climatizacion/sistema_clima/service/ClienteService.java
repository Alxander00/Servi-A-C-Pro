package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.ClienteEntity;

import java.util.List;
import java.util.Optional;

public interface ClienteService {

    ClienteEntity crearCliente(ClienteEntity cliente);

    Optional<ClienteEntity> obtenerPorId(Long idCliente);

    List<ClienteEntity> obtenerClientesActivos();

    ClienteEntity actualizarCliente(Long idCliente, ClienteEntity clienteActualizado);

    void desactivarCliente(Long idCliente);
}
