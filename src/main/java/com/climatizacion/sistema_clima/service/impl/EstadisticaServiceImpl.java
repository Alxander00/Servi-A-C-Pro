package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.EstadisticasClienteDTO;
import com.climatizacion.sistema_clima.dto.EstadisticasDTO;
import com.climatizacion.sistema_clima.enums.Rol;
import com.climatizacion.sistema_clima.repository.PedidoRepository;
import com.climatizacion.sistema_clima.repository.ProductoRepository;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.EstadisticaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticaServiceImpl implements EstadisticaService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public EstadisticasDTO obtenerDashboard() {
        // Ventas totales
        Query queryVentas = entityManager.createQuery(
                "SELECT COALESCE(SUM(p.total), 0.0) FROM PedidoEntity p WHERE p.estado = 'Completado'");
        Object ventasObj = queryVentas.getSingleResult();
        BigDecimal ventasTotales = toBigDecimal(ventasObj);

        long totalPedidos = pedidoRepository.count();
        long totalProductos = productoRepository.countByActivoTrue();
        // ✅ FIX: Solo contar CLIENTES, no todos los usuarios
        long totalClientes = usuarioRepository.countByRol(Rol.CLIENTE);

        // ✅ FIX: TO_CHAR en vez de DATE_FORMAT (PostgreSQL)
        Query ventasPorMesQuery = entityManager.createNativeQuery(
                "SELECT TO_CHAR(fecha_pedido, 'YYYY-MM') as mes, SUM(total) as total " +
                        "FROM pedidos WHERE estado = 'Completado' " +
                        "GROUP BY mes ORDER BY mes DESC LIMIT 12");
        List<Object[]> ventasPorMesRaw = ventasPorMesQuery.getResultList();
        List<Map<String, Object>> ventasPorMes = ventasPorMesRaw.stream()
                .map(row -> Map.of("mes", row[0], "total", ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());

        Query topProductosQuery = entityManager.createNativeQuery(
                "SELECT p.nombre, SUM(dp.cantidad) as total_vendido " +
                        "FROM detalle_pedido dp JOIN productos p ON dp.id_producto = p.id_producto " +
                        "JOIN pedidos ped ON dp.id_pedido = ped.id_pedido " +
                        "WHERE ped.estado = 'Completado' " +
                        "GROUP BY p.id_producto, p.nombre ORDER BY total_vendido DESC LIMIT 5");
        List<Object[]> topProductosRaw = topProductosQuery.getResultList();
        List<Map<String, Object>> productosMasVendidos = topProductosRaw.stream()
                .map(row -> Map.of("nombre", row[0], "vendidos", ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        Query estadosQuery = entityManager.createQuery(
                "SELECT p.estado, COUNT(p) FROM PedidoEntity p GROUP BY p.estado");
        List<Object[]> estadosRaw = estadosQuery.getResultList();
        Map<String, Long> pedidosPorEstado = estadosRaw.stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));

        return EstadisticasDTO.builder()
                .ventasTotales(ventasTotales)
                .totalPedidos(totalPedidos)
                .totalProductos(totalProductos)
                .totalClientes(totalClientes)
                .ventasPorMes(ventasPorMes)
                .productosMasVendidos(productosMasVendidos)
                .pedidosPorEstado(pedidosPorEstado)
                .build();
    }

    @Override
    public EstadisticasClienteDTO obtenerEstadisticasCliente(Long idCliente) {
        // 1. Gastos por mes — ✅ FIX: TO_CHAR en vez de DATE_FORMAT
        Query gastosPorMesQuery = entityManager.createNativeQuery(
                        "SELECT TO_CHAR(fecha_pedido, 'YYYY-MM') as mes, SUM(total) as total " +
                                "FROM pedidos WHERE id_cliente = :idCliente AND estado = 'Completado' " +
                                "GROUP BY mes ORDER BY mes DESC LIMIT 12")
                .setParameter("idCliente", idCliente);
        List<Object[]> gastosRaw = gastosPorMesQuery.getResultList();
        List<Map<String, Object>> gastosPorMes = gastosRaw.stream()
                .map(row -> Map.of("mes", row[0], "total", ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());

        // 2. Productos más comprados por este cliente
        Query topProductosClienteQuery = entityManager.createNativeQuery(
                        "SELECT p.nombre, SUM(dp.cantidad) as total_comprado " +
                                "FROM detalle_pedido dp JOIN productos p ON dp.id_producto = p.id_producto " +
                                "JOIN pedidos ped ON dp.id_pedido = ped.id_pedido " +
                                "WHERE ped.id_cliente = :idCliente AND ped.estado = 'Completado' " +
                                "GROUP BY p.id_producto, p.nombre ORDER BY total_comprado DESC LIMIT 5")
                .setParameter("idCliente", idCliente);
        List<Object[]> topProductosRaw = topProductosClienteQuery.getResultList();
        List<Map<String, Object>> productosMasComprados = topProductosRaw.stream()
                .map(row -> Map.of("nombre", row[0], "comprados", ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        // 3. Total de pedidos
        Query totalPedidosQuery = entityManager.createQuery(
                        "SELECT COUNT(p) FROM PedidoEntity p WHERE p.idUsuario = :idCliente AND p.estado = 'Completado'")
                .setParameter("idCliente", idCliente);
        Long totalPedidos = (Long) totalPedidosQuery.getSingleResult();

        // 4. Total gastado — ✅ FIX: SUM en Postgres devuelve BigDecimal, NO Double
        Query totalGastadoQuery = entityManager.createQuery(
                        "SELECT COALESCE(SUM(p.total), 0.0) FROM PedidoEntity p WHERE p.idUsuario = :idCliente AND p.estado = 'Completado'")
                .setParameter("idCliente", idCliente);
        Double totalGastado = toBigDecimal(totalGastadoQuery.getSingleResult()).doubleValue();

        return EstadisticasClienteDTO.builder()
                .gastosPorMes(gastosPorMes)
                .productosMasComprados(productosMasComprados)
                .totalPedidos(totalPedidos)
                .totalGastado(totalGastado)
                .build();
    }

    /**
     * Convierte de forma segura cualquier Number/BigDecimal a BigDecimal.
     * Necesario porque PostgreSQL devuelve BigDecimal en SUM(), mientras que
     * MySQL devolvía Double.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return BigDecimal.ZERO;
    }
}