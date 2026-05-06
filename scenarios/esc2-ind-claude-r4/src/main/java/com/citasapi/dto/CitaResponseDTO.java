package com.citasapi.dto;

import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;

    // ── Constructor de mapeo ─────────────────────────────

    public CitaResponseDTO(Cita cita) {
        this.id            = cita.getId();
        this.nombreCliente = cita.getNombreCliente();
        this.fecha         = cita.getFecha();
        this.hora          = cita.getHora();
        this.motivo        = cita.getMotivo();
        this.estado        = cita.getEstado();
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }
    public String getNombreCliente() { return nombreCliente; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getMotivo() { return motivo; }
    public EstadoCita getEstado() { return estado; }
}