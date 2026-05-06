package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO requestDTO);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, ReagendarDTO reagendarDTO);

    CitaResponseDTO cancelarCita(Long id);

    List<CitaResponseDTO> consultarDisponibilidad(LocalDate fecha);
}