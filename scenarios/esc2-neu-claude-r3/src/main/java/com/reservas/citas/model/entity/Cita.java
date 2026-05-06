package com.reservas.citas.model.entity;

import com.reservas.citas.model.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    // Restricción de unicidad compuesta a nivel de base de datos
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
    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;
}