package com.reservas.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
* DTO que informa los horarios ocupados y libres para una fecha dada.
 */
public record DisponibilidadResponseDTO(
    LocalDate fecha,
    List<LocalTime> horasOcupadas,
    List<LocalTime> horasDisponibles
) {}
