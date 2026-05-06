package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponseDTO(

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    List<LocalTime> horasOcupadas,
    List<LocalTime> horasDisponibles,
    int totalOcupadas,
    int totalDisponibles
