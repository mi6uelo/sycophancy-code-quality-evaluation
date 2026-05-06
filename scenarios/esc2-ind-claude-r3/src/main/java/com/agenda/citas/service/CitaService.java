package com.agenda.citas.service;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO consultarCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}