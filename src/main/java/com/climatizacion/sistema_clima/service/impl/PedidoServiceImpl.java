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
import com.climatizacion.sistema_clima.service.ResendEmailService;
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
    private final UsuarioRepository usuarioRepository;
    private final ResendEmailService resendEmailService;

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
            // Validar que el producto existe (para lanzar error si no)
            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Descontar stock en la base de datos
            int rowsUpdated = productoRepository.descontarStock(item.getIdProducto(), item.getCantidad());
            if (rowsUpdated == 0) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Crear el detalle del pedido
            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedidoGuardado);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(BigDecimal.valueOf(item.getPrecioUnitario()));
            detallePedidoRepository.save(detalle);
        }

        // Notificación por correo
        try {
            UsuarioEntity usuario = usuarioRepository.findById(Long.valueOf(pedidoGuardado.getIdUsuario()))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            resendEmailService.enviarCorreoPedidoCreado(usuario.getEmail(), usuario.getNombres(), pedidoGuardado.getIdPedido());
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo enviar correo de confirmación de pedido #" + pedidoGuardado.getIdPedido() + " - " + e.getMessage());
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
    public PedidoEntity obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<PedidoEntity> listarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        PedidoEntity pedido = obtenerPorId(id);
        String estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);
        repository.save(pedido);

        try {
            UsuarioEntity usuario = usuarioRepository.findById(Long.valueOf(pedido.getIdUsuario()))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            resendEmailService.enviarCorreoCambioEstadoPedido(usuario.getEmail(), usuario.getNombres(),
                    id, estadoAnterior, nuevoEstado);
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo enviar correo de cambio de estado del pedido #" + id + " - " + e.getMessage());
        }
    }

    @Override
    public long contarPedidosPorEstado(List<String> estados) {
        return repository.countByEstadoIn(estados);
    }

    @Override
    public long contarPedidosPorEstadoYUsuario(Long idUsuario, List<String> estados) {
        return repository.countByIdUsuarioAndEstadoIn(idUsuario, estados);
    }
}