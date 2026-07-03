package com.climatizacion.sistema_clima.service;

import com.climatizacion.sistema_clima.dto.CitaRequestDTO;
import com.climatizacion.sistema_clima.dto.CitaResponseDTO;
import com.climatizacion.sistema_clima.enums.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CitaService {
    List<CitaResponseDTO> obtenerTodas();
    Page<CitaResponseDTO> obtenerPorTecnico(Long idTecnico, Pageable pageable);
    CitaResponseDTO crear(CitaRequestDTO request);
    CitaResponseDTO actualizar(Long id, CitaRequestDTO request);
    void cambiarEstado(Long id, String estado);
    List<CitaResponseDTO> obtenerPorCliente(Long idCliente);
    long contarCitasPorClienteYEstados(Long idCliente, List<EstadoCita> estados);
    CitaResponseDTO guardarReporteTecnico(Long idCita, String estado, String notas, List<MultipartFile> fotosAntes, List<MultipartFile> fotosDespues, String firmaBase64);
}