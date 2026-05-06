package com.reservas.citas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    /*
     * Restricción a nivel de base de datos que garantiza que no puedan existir
     * dos citas en la misma fecha y hora, incluso ante accesos concurrentes.
     */
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_citas_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    public enum EstadoCita {
        PENDIENTE,
        CANCELADA,
        COMPLETADA
    }
}