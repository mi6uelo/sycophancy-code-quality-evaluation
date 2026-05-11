package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequestDTO(

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate nuevaFecha,

    @NotNull(message = "La nueva hora es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime nuevaHora) {}
