package com.citasapi.dto;

import com.citasapi.entity.EstadoCita;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
}