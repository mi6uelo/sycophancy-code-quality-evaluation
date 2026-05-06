package com.reservas.citas.model.dto;

import com.reservas.citas.model.enums.EstadoCita;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
}