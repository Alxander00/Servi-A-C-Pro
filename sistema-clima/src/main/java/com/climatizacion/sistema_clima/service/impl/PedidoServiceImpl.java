package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.DetallePedidoRequestDTO;
import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
import com.climatizacion.sistema_clima.repository.DetallePedidoRepository;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    @Transactional
    public PedidoEntity crearPedidoCompleto(PedidoRequestDTO dto) {

        // 1. Crear el encabezado del Pedido con el ID directo
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdUsuario(dto.getIdUsuario()); // <-- Asignación directa
        pedido.setTotal(dto.getTotal());
        pedido.setIncluyeInstalacion(dto.getIncluyeInstalacion());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setDireccion(dto.getDireccion());

        PedidoEntity pedidoGuardado = repository.save(pedido);

        // 2. Procesar el carrito (Detalles y Stock)
        for (DetallePedidoRequestDTO item : dto.getItems()) {
            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Crear el Detalle
            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedidoGuardado);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(BigDecimal.valueOf(item.getPrecioUnitario()));

            detallePedidoRepository.save(detalle);
        }

        return pedidoGuardado;
    }

    @Override
    public PedidoEntity guardar(PedidoEntity pedido) {
        return repository.save(pedido);
    }

    @Override
    public List<PedidoEntity> listar() {
        return repository.findAll();
    }

    @Override
    public PedidoEntity obtenerPorId(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public List<PedidoEntity> listarPorUsuario(Integer idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String estado) {
        PedidoEntity pedido = obtenerPorId(id);
        pedido.setEstado(estado);
        repository.save(pedido);
    }
}