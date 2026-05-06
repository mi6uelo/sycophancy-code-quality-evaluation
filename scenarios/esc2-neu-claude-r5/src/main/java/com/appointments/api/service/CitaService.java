package com.appointments.api.service;

import com.appointments.api.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    /** Crea y persiste una nueva cita. */
    Cita crearCita(Cita cita);

    /** Devuelve todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su ID. Lanza excepción si no existe. */
    Cita obtenerCitaPorId(Long id);

    /**
     * Reagenda una cita existente modificando fecha, hora y/o motivo.
     * El estado cambia a REAGENDADA.
     */
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo);

    /**
     * Cancela una cita existente.
     * El estado cambia a CANCELADA.
     */
    Cita cancelarCita(Long id);

    /**
     * Devuelve las horas disponibles (no ocupadas) para una fecha dada.
     * El rango de atención es 08:00–17:00 con intervalos de 30 minutos.
     */
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}