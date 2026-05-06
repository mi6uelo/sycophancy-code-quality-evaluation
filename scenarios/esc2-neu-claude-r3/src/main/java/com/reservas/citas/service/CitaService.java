package com.reservas.citas.service;

import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    /** Crea una nueva cita y la persiste en estado PROGRAMADA. */
    CitaResponseDTO crearCita(CitaRequestDTO request);

    /** Retorna todas las citas registradas. */
    List<CitaResponseDTO> listarCitas();

    /** Busca una cita por su ID. */
    CitaResponseDTO obtenerCitaPorId(Long id);

    /** Reagenda una cita existente con nueva fecha y hora. */
    CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto);

    /** Cancela una cita existente. */
    CitaResponseDTO cancelarCita(Long id);

    /**
     * Consulta las horas disponibles para una fecha dada, dentro de un rango
     * de atención (09:00 – 17:00) con intervalos de 30 minutos.
     */
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}