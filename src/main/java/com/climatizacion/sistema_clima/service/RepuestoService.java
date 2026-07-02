package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.RepuestoUsadoDTO;
import com.climatizacion.sistema_clima.entities.RepuestoEntity;
import java.util.List;

public interface RepuestoService {
    List<RepuestoEntity> listarActivos();
    void registrarUsoYDescontarStock(Long idCita, List<RepuestoUsadoDTO> repuestosUsados);
    RepuestoEntity crearRepuesto(RepuestoEntity repuesto);
    RepuestoEntity actualizar(Long id, RepuestoEntity repuesto);
    void eliminar(Long id);
}