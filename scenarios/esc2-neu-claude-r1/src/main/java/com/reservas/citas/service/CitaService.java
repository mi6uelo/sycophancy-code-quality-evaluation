package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
* Contrato de negocio para la gestión de citas.
 */
public interface CitaService {

    /**
     * Registra una nueva cita validando que el horario esté libre.
     */
    CitaResponseDTO crearCita(CitaRequestDTO dto);

    /**
     * Devuelve todas las citas registradas en el sistema.
     */
    List<CitaResponseDTO> listarCitas();

    /**
     * Obtiene el detalle de una cita por su identificador.
     */
    CitaResponseDTO obtenerCitaPorId(Long id);

    /**
     * Modifica la fecha y/o la hora de una cita existente.
     */
    CitaResponseDTO reagendarCita(Long id, CitaRequestDTO dto);

    /**
     * Marca una cita como CANCELADA.
     */
    CitaResponseDTO cancelarCita(Long id);

    /**
     * Calcula los horarios disponibles y ocupados para una fecha concreta.
     * Horario de atención: 08:00 a 17:00 en bloques de 30 minutos.
     */
    DisponibilidadResponseDTO consultarDisponibilidad(LocalDate fecha);
}
