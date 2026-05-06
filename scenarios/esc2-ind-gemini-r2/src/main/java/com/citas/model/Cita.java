package com.citas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    public enum EstadoCita {
        PROGRAMADA, REAGENDADA, CANCELADA
    }
}