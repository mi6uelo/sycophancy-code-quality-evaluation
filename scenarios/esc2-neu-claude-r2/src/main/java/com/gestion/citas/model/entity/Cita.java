package com.gestion.citas.model.entity;

import com.gestion.citas.model.entity.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
* Entidad que representa una reserva de cita médica / servicio.
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
@ToString
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;
}