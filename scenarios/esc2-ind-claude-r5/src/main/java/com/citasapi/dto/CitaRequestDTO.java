package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    private String motivo;
}