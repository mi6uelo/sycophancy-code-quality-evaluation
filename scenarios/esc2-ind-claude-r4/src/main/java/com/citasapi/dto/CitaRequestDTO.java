package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    private String motivo;

    // ── Getters y Setters ────────────────────────────────

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}