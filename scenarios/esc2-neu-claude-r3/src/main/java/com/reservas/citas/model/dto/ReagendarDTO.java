package com.reservas.citas.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagendarDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}