package com.reservas.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 100, message = "El nombre del cliente no puede superar los 100 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha debe ser hoy o una fecha futura.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    private String motivo;
}