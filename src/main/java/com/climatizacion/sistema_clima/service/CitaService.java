package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CitaService {
    List<CitaResponseDTO> obtenerTodas();
    List<CitaResponseDTO> obtenerPorTecnico(Long idTecnico);
    CitaResponseDTO crear(CitaRequestDTO request);
    CitaResponseDTO actualizar(Long id, CitaRequestDTO request);
    void cambiarEstado(Long id, String estado);
    List<CitaResponseDTO> obtenerPorCliente(Long idCliente);
    long contarCitasPorClienteYEstados(Long idCliente, List<EstadoCita> estados);
    CitaResponseDTO guardarReporte(Long idCita, String estado, String notas, List<MultipartFile> fotosAntes, List<MultipartFile> fotosDespues, String firmaBase64);
}