package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.DetallePedidoDTO;
import com.climatizacion.sistema_clima.entities.*;
import com.climatizacion.sistema_clima.repository.*;
import com.climatizacion.sistema_clima.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetallePedidoServiceImpl implements DetallePedidoService {

    private final DetallePedidoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    @Override
    public DetallePedidoDTO guardar(DetallePedidoDTO dto) {

        DetallePedidoEntity detalle = toEntity(dto);

        PedidoEntity pedido = pedidoRepository.findById(detalle.getPedido().getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        ProductoEntity producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        detalle.setPedido(pedido);
        detalle.setProducto(producto);


        detalle.setPrecioUnitario(producto.getPrecio());

        DetallePedidoEntity guardado = repository.save(detalle);

        return toDTO(guardado);
    }

    @Override
    public List<DetallePedidoDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }


    private DetallePedidoDTO toDTO(DetallePedidoEntity entity) {
        DetallePedidoDTO dto = new DetallePedidoDTO();

        dto.setIdDetalle(entity.getIdDetalle());
        dto.setIdPedido(entity.getPedido().getIdPedido());
        dto.setIdProducto(entity.getProducto().getIdProducto());
        dto.setCantidad(entity.getCantidad());
        dto.setPrecioUnitario(entity.getPrecioUnitario());

        return dto;
    }

    private DetallePedidoEntity toEntity(DetallePedidoDTO dto) {
        DetallePedidoEntity entity = new DetallePedidoEntity();

        entity.setIdDetalle(dto.getIdDetalle());

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdPedido(dto.getIdPedido());

        ProductoEntity producto = new ProductoEntity();
        producto.setIdProducto(dto.getIdProducto());

        entity.setPedido(pedido);
        entity.setProducto(producto);
        entity.setCantidad(dto.getCantidad());

        return entity;
    }
}