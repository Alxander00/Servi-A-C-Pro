package com.climatizacion.sistema_clima.service.impl;

import com.climatizacion.sistema_clima.dto.CategoriaRequestDTO;
import com.climatizacion.sistema_clima.dto.CategoriaResponseDTO;
import com.climatizacion.sistema_clima.entities.CategoriaEntity;
import com.climatizacion.sistema_clima.repository.CategoriaRepository;
import com.climatizacion.sistema_clima.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO request){
        if (categoriaRepository.existsByNombre(request.getNombre())){
            throw new RuntimeException("Ya existe una categoria con este nombre");
        }
        CategoriaEntity nuevaCategoria = new CategoriaEntity();
        nuevaCategoria.setNombre(request.getNombre());

        if (request.getIdCategoriaPadre()!=null){
            CategoriaEntity padre = categoriaRepository.findById(request.getIdCategoriaPadre())
                    .orElseThrow(() -> new RuntimeException("La categoria no existe"));
            nuevaCategoria.setCategoriaPadre(padre);
        }

        CategoriaEntity guardada = categoriaRepository.save(nuevaCategoria);
        return mapearAResponseDTO(guardada);
    }

    @Override
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaResponseDTO obtenerPorId(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return mapearAResponseDTO(categoria);
    }

    @Override
    public void eliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }

    private CategoriaResponseDTO mapearAResponseDTO(CategoriaEntity entidad) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setIdCategoria(entidad.getIdCategoria());
        dto.setNombre(entidad.getNombre());

        if (entidad.getCategoriaPadre() != null) {
            dto.setIdCategoriaPadre(entidad.getCategoriaPadre().getIdCategoria());
            dto.setNombreCategoriaPadre(entidad.getCategoriaPadre().getNombre());
        }
        return dto;
    }
}
