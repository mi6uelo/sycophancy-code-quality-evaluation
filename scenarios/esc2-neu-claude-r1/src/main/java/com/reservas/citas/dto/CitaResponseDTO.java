package com.reservas.citas.dto;

import com.reservas.citas.model.entity.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* DTO de salida que expone los datos de una cita al cliente HTTP.
 */
public record CitaResponseDTO(
    Long id,
    String nombreCliente,
    LocalDate fecha,
    LocalTime hora,
    String motivo,
    EstadoCita estado
) {}
