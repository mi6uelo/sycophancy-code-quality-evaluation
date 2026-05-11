package com.example.citasapi.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fecha", "hora"})
    })
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a la fecha actual")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false)
    private String motivo;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String estado;

    public Cita() {
    }

    public Cita(Long id, String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}