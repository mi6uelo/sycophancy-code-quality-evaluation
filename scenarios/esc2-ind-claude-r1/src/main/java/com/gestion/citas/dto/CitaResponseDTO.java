package com.gestion.citas.dto;

import com.gestion.citas.entity.Cita.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Representación de una cita que se devuelve al cliente REST.
 */
@Data
@Builder
public class CitaResponseDTO {

    private Long        id;
    private String      nombreCliente;
    private LocalDate   fecha;
    private LocalTime   hora;
    private String      motivo;
    private EstadoCita  estado;
}