package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, CitaRequestDTO request);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}