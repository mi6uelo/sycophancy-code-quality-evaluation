package com.agenda.citas.entity;

import com.agenda.citas.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    /*
     * Restricción única a nivel de base de datos:
     * no puede existir más de una cita con la misma fecha y hora.
     */
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cita_fecha_hora",
        columnNames = {"fecha", "hora"}
    )
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

    @Column(name = "nombre_cliente", nullable = false, length = 150)
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
}