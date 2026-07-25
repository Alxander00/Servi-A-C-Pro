package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.*;
import com.climatizacion.sistema_clima.entities.*;
import com.climatizacion.sistema_clima.repository.DetallePedidoRepository;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.PedidoService;
import com.climatizacion.sistema_clima.service.ResendEmailService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de pedidos.
 */
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResendEmailService resendEmailService;

    /**
     * Crea un pedido completo con sus detalles.
     * @param dto Datos del pedido (usuario, items, total, dirección)
     * @return Entidad del pedido creado
     * @throws RuntimeException si el usuario no existe, o algún producto no tiene stock
     */
    @Override
    @Transactional
    public PedidoEntity crearPedidoCompleto(PedidoRequestDTO dto) {
        // ✅ Validar que el usuario exista
        UsuarioEntity usuario = usuarioRepository.findById(Long.valueOf(dto.getIdUsuario()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new RuntimeException("El usuario no está activo en el sistema");
        }

        // ✅ Validar que la dirección no esté vacía
        if (dto.getDireccion() == null || dto.getDireccion().trim().isEmpty()) {
            throw new RuntimeException("La dirección de instalación es obligatoria");
        }

        // ✅ Validar que el total no sea negativo
        if (dto.getTotal() == null || dto.getTotal() < 0) {
            throw new RuntimeException("El total del pedido no puede ser negativo");
        }

        // ✅ Validar que haya al menos un item
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos un producto");
        }

        PedidoEntity pedido = new PedidoEntity();
        pedido.setIdUsuario(dto.getIdUsuario());
        pedido.setTotal(dto.getTotal());
        pedido.setIncluyeInstalacion(dto.getIncluyeInstalacion());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setDireccion(dto.getDireccion());

        PedidoEntity pedidoGuardado = repository.save(pedido);

        // Procesar cada item
        for (DetallePedidoRequestDTO item : dto.getItems()) {
            // ✅ Validar que el producto exista
            ProductoEntity producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // ✅ Validar que la cantidad sea positiva
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad del producto " + producto.getNombre() + " debe ser mayor a cero");
            }

            // Descontar stock
            int rowsUpdated = productoRepository.descontarStock(item.getIdProducto(), item.getCantidad());
            if (rowsUpdated == 0) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                        ". Disponible: " + producto.getStock());
            }

            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedidoGuardado);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(BigDecimal.valueOf(item.getPrecioUnitario()));
            detallePedidoRepository.save(detalle);
        }

        // Enviar correo de confirmación
        try {
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
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<PedidoEntity> listarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    /**
     * Cambia el estado de un pedido y notifica al usuario por correo.
     * @param id ID del pedido
     * @param nuevoEstado Nuevo estado (Pendiente, En Proceso, Completado, Cancelado)
     */
    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        PedidoEntity pedido = obtenerPorId(id);
        String estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);
        repository.save(pedido);

        // Notificar al usuario
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

    @Override
    public List<PedidoEntity> listarConFiltros(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                               String estado, Long idCliente, String emailCliente) {
        Specification<PedidoEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaPedido"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaPedido"), fechaFin));
            }
            if (estado != null && !estado.isEmpty()) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (idCliente != null) {
                predicates.add(cb.equal(root.get("idUsuario"), idCliente));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec);
    }

    @Override
    public Page<PedidoEntity> listarPorUsuarioPaginado(Long idUsuario, Pageable pageable) {
        return repository.findByIdUsuario(idUsuario, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> obtenerPedidosPaginados(String search, String estado, Pageable pageable) {
        return repository.buscarConFiltros(search, estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDetalleResponseDTO obtenerPedidoConDetalles(Long id) {
        // 1. Obtener el pedido (sin detalles)
        PedidoEntity pedido = obtenerPorId(id);

        // 2. Obtener los detalles con productos e imágenes (usando el nuevo método)
        List<DetallePedidoEntity> detalles = detallePedidoRepository.findByPedidoIdWithProductos(id);

        // 3. Datos del cliente
        String nombreCliente = "";
        String fotoUrl = null;
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findById(pedido.getIdUsuario());
        if (usuarioOpt.isPresent()) {
            UsuarioEntity u = usuarioOpt.get();
            nombreCliente = u.getNombres() + " " + (u.getApellidos() != null ? u.getApellidos() : "");
            fotoUrl = u.getFotoUrl();
        }

        // 4. Mapear detalles a DTO
        List<DetallePedidoResponseDTO> detallesDTO = detalles.stream()
                .map(d -> {
                    ProductoEntity producto = d.getProducto();
                    List<String> imagenesUrls = producto.getImagenes().stream()
                            .map(ProductoImagen::getImagenUrl)
                            .collect(Collectors.toList());

                    return DetallePedidoResponseDTO.builder()
                            .idDetalle(d.getIdDetalle())
                            .idProducto(producto.getIdProducto())
                            .nombreProducto(producto.getNombre())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .capacidadBtu(producto.getCapacidadBtu())
                            .imagenesUrls(imagenesUrls)
                            .build();
                })
                .collect(Collectors.toList());

        // 5. Construir respuesta
        return PedidoDetalleResponseDTO.builder()
                .idPedido(pedido.getIdPedido())
                .idUsuario(pedido.getIdUsuario())
                .nombreCliente(nombreCliente)
                .fotoUrl(fotoUrl)
                .fechaPedido(pedido.getFechaPedido())
                .total(pedido.getTotal())
                .incluyeInstalacion(pedido.getIncluyeInstalacion())
                .estado(pedido.getEstado())
                .direccion(pedido.getDireccion())
                .detalles(detallesDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPorUsuarioPaginadoDTO(Long idUsuario, Pageable pageable) {
        return repository.findPedidosByUsuarioDto(idUsuario, pageable);
    }
}