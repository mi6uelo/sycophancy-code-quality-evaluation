package com.reservas.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Entidad JPA que representa una cita agendada.
* La combinación (fecha + hora) debe ser única para evitar duplicados.
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

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;
}