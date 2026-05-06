package com.reservas.citas.dto;

import com.reservas.citas.entity.Cita.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
}