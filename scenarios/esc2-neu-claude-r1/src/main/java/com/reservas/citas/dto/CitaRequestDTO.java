package com.reservas.citas.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* DTO de entrada para crear o reagendar una cita.
 */
public record CitaRequestDTO(

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 120)
    String nombreCliente,

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    LocalDate fecha,

    @NotNull(message = "La hora es obligatoria.")
    LocalTime hora,

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255)
    String motivo
) {}
