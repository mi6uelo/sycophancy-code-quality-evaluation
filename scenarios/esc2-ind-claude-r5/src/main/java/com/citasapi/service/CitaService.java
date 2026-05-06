package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}