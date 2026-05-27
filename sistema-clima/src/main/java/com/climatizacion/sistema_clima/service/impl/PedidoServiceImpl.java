package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.DetallePedidoRequestDTO;
import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.DetallePedidoEntity;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.ProductoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.DetallePedidoRepository;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.EmailService;
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
    private final UsuarioRepository usuarioRepository;   // para obtener datos del cliente
    private final EmailService emailService;           // para enviar correos

    @Override
    @Transactional
    public PedidoEntity crearPedidoCompleto(PedidoRequestDTO dto) {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdUsuario(dto.getIdUsuario());
        pedido.setTotal(dto.getTotal());
        pedido.setIncluyeInstalacion(dto.getIncluyeInstalacion());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setDireccion(dto.getDireccion());

        PedidoEntity pedidoGuardado = repository.save(pedido);

        for (DetallePedidoRequestDTO item : dto.getItems()) {
            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedidoGuardado);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(BigDecimal.valueOf(item.getPrecioUnitario()));
            detallePedidoRepository.save(detalle);
        }

        // ✅ NOTIFICAR AL CLIENTE QUE EL PEDIDO HA SIDO CREADO
        UsuarioEntity usuario = usuarioRepository.findById(Long.valueOf(pedidoGuardado.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        emailService.enviarCorreoPedidoCreado(usuario.getEmail(), usuario.getNombres(), pedidoGuardado.getIdPedido());

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
    public void cambiarEstado(Integer id, String nuevoEstado) {
        PedidoEntity pedido = obtenerPorId(id);
        String estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);
        repository.save(pedido);

        // ✅ NOTIFICAR AL CLIENTE EL CAMBIO DE ESTADO
        UsuarioEntity usuario = usuarioRepository.findById(Long.valueOf(pedido.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        emailService.enviarCorreoCambioEstadoPedido(usuario.getEmail(), usuario.getNombres(),
                id, estadoAnterior, nuevoEstado);
    }

    // En PedidoServiceImpl
    @Override
    public long contarPedidosPorEstado(List<String> estados) {
        return repository.countByEstadoIn(estados);
    }

    @Override
    public long contarPedidosPorEstadoYUsuario(Integer idUsuario, List<String> estados) {
        return repository.countByIdUsuarioAndEstadoIn(idUsuario, estados);
    }
}