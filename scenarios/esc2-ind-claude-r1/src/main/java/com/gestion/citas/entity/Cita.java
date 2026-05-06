package com.gestion.citas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Entidad JPA que representa una cita médica/servicio.
* La restricción UNIQUE sobre (fecha, hora) garantiza a nivel de base de datos
* que no existan citas duplicadas en el mismo horario.
 */
@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cita_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    // ─── Estados posibles de una cita ────────────────────
    public enum EstadoCita {
        PENDIENTE,
        CANCELADA,
        COMPLETADA
    }
}