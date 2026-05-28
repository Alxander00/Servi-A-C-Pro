package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.entities.ServicioEntity;
import java.util.List;

public interface ServicioService {
    ServicioEntity guardar(ServicioEntity servicio);
    List<ServicioEntity> listar();
    ServicioEntity obtenerPorId(Long id);
    void eliminar(Long id);
}