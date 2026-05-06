package com.appointments.model.dto;

import com.appointments.model.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDTO(
    Long id,
    String nombreCliente,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    @JsonFormat(pattern = "HH:mm")
    LocalTime hora,

    String motivo,
    AppointmentStatus estado