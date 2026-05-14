package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.service.PedidoService;
import com.climatizacion.sistema_clima.repository.ClienteRepository;
import com.climatizacion.sistema_clima.entities.ClienteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final ClienteRepository clienteRepository;

    @Override
    public PedidoEntity guardar(PedidoEntity pedido) {


        if (pedido.getCliente() == null || pedido.getCliente().getIdCliente() == null) {
            throw new RuntimeException("Debe enviar el id del cliente");
        }


        Long idCliente = pedido.getCliente().getIdCliente();

        ClienteEntity cliente = clienteRepository.findById(Long.valueOf(idCliente))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));


        pedido.setCliente(cliente);

        return repository.save(pedido);
    }

    @Override
    public List<PedidoEntity> listar() {
        return repository.findAll();
    }

    @Override
    public PedidoEntity obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}