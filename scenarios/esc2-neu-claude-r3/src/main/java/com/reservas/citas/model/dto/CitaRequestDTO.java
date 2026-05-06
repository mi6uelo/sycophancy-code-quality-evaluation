package com.reservas.citas.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    private String motivo;
}