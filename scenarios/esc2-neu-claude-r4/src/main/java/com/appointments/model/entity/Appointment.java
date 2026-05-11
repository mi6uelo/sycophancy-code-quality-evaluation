package com.appointments.model.entity;

import com.appointments.model.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_appointment_date_time",
            columnNames = {"fecha", "hora"}
        )
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus estado = AppointmentStatus.PENDIENTE;
}