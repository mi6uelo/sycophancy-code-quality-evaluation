package com.agenda.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha debe ser hoy o una fecha futura.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 300, message = "El motivo no puede superar los 300 caracteres.")
    private String motivo;
}