package com.gestion.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Payload para modificar la fecha y/u hora de una cita existente.
 */
@Data
public class ReagendarRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}