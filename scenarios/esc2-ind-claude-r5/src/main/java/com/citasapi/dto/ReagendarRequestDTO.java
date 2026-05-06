package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReagendarRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}