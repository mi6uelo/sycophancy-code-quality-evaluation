package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequestDTO(

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    String nombreCliente,

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    @NotNull(message = "La hora es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime hora,

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres.")
    String motivo) {}
