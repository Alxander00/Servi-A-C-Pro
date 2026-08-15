package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.RepuestoUsadoDTO;
import com.climatizacion.sistema_clima.entities.CitaEntity;
import com.climatizacion.sistema_clima.entities.RepuestoEntity;
import com.climatizacion.sistema_clima.entities.UsoRepuestoEntity;
import com.climatizacion.sistema_clima.exceptions.CitaNotFoundException;
import com.climatizacion.sistema_clima.exceptions.StockInsuficienteException;
import com.climatizacion.sistema_clima.repository.CitaRepository;
import com.climatizacion.sistema_clima.repository.RepuestoRepository;
import com.climatizacion.sistema_clima.repository.UsoRepuestoRepository;
import com.climatizacion.sistema_clima.service.RepuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepuestoServiceImpl implements RepuestoService {

    private final RepuestoRepository repuestoRepository;
    private final UsoRepuestoRepository usoRepuestoRepository;
    private final CitaRepository citaRepository;

    @Override
    public List<RepuestoEntity> listarActivos() {
        return repuestoRepository.findByActivoTrue();
    }

    @Override
    @Transactional // CRÍTICO: Si falla un repuesto, se cancela todo el descuento para evitar errores de inventario
    public void registrarUsoYDescontarStock(Long idCita, List<RepuestoUsadoDTO> repuestosUsados) {
        if (repuestosUsados == null || repuestosUsados.isEmpty()) {
            return; // El técnico no usó repuestos, no hacemos nada
        }

        CitaEntity cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNotFoundException("Cita no encontrada para registrar repuestos"));

        for (RepuestoUsadoDTO dto : repuestosUsados) {
            RepuestoEntity repuesto = repuestoRepository.findById(dto.getIdRepuesto())
                    .orElseThrow(() -> new RuntimeException("Repuesto no encontrado en el almacén"));

            // Validar que tengamos suficiente stock
            if (repuesto.getStockActual() < dto.getCantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para: " + repuesto.getNombre() +
                        ". Intentas usar " + dto.getCantidad() + " pero solo quedan " + repuesto.getStockActual());
            }

            // 1. Descontar del inventario maestro
            repuesto.setStockActual(repuesto.getStockActual() - dto.getCantidad());
            repuestoRepository.save(repuesto);

            // 2. Dejar el registro en el historial de la cita
            UsoRepuestoEntity uso = UsoRepuestoEntity.builder()
                    .cita(cita)
                    .repuesto(repuesto)
                    .cantidadUtilizada(dto.getCantidad())
                    .fechaRegistro(LocalDateTime.now())
                    .build();
            usoRepuestoRepository.save(uso);
        }
    }

    @Override
    public RepuestoEntity crearRepuesto(RepuestoEntity repuesto) {
        repuesto.setActivo(true);
        return repuestoRepository.save(repuesto);
    }

    @Override
    public RepuestoEntity actualizar(Long id, RepuestoEntity repuestoActualizado) {
        // 1. Buscamos si el repuesto existe
        RepuestoEntity repuestoExistente = repuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado en el inventario"));

        // 2. Actualizamos solo los datos permitidos
        repuestoExistente.setNombre(repuestoActualizado.getNombre());
        repuestoExistente.setUnidadMedida(repuestoActualizado.getUnidadMedida());
        repuestoExistente.setStockActual(repuestoActualizado.getStockActual());
        repuestoExistente.setCostoUnitario(repuestoActualizado.getCostoUnitario());

        // 3. Guardamos los cambios
        return repuestoRepository.save(repuestoExistente);
    }

    @Override
    public void eliminar(Long id) {
        RepuestoEntity repuesto = repuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        repuesto.setActivo(false);

        repuestoRepository.save(repuesto);
    }
}