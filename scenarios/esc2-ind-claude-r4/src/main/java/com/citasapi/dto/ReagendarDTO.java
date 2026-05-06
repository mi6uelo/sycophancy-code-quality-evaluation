package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReagendarDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;

    // ── Getters y Setters ────────────────────────────────

    public LocalDate getNuevaFecha() { return nuevaFecha; }
    public void setNuevaFecha(LocalDate nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public LocalTime getNuevaHora() { return nuevaHora; }
    public void setNuevaHora(LocalTime nuevaHora) {
        this.nuevaHora = nuevaHora;
    }
}