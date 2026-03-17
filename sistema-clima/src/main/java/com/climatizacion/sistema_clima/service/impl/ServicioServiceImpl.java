package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.entities.ServicioEntity;
import com.climatizacion.sistema_clima.repository.ServicioRepository;
import com.climatizacion.sistema_clima.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository repository;

    @Override
    public ServicioEntity guardar(ServicioEntity servicio) {
        return repository.save(servicio);
    }

    @Override
    public List<ServicioEntity> listar() {
        return repository.findAll();
    }

    @Override
    public ServicioEntity obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}