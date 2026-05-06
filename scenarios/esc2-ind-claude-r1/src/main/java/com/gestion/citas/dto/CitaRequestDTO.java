package com.gestion.citas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Payload de entrada para crear una nueva cita.
 */
@Data
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres.")
    private String motivo;
}