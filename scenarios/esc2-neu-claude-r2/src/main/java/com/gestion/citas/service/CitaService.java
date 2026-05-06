package com.gestion.citas.service;

import com.gestion.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface CitaService {

    /** Crea y persiste una nueva cita. */
    Cita crearCita(Cita cita);

    /** Retorna todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su ID. Lanza excepción si no existe. */
    Cita obtenerCitaPorId(Long id);

    /**
     * Reagenda una cita existente: cambia fecha y/u hora.
     * Valida que el nuevo slot no esté ocupado.
     *
     * @param id        ID de la cita a reagendar.
     * @param nuevaFecha Nueva fecha deseada.
     * @param nuevaHora  Nueva hora deseada.
     * @return Cita actualizada con estado REAGENDADA.
     */
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     * No es posible cancelar una cita ya cancelada o completada.
     */
    Cita cancelarCita(Long id);

    /**
     * Consulta la disponibilidad de horarios para una fecha dada.
     * Retorna un mapa con:
     *   "fecha"          → fecha consultada
     *   "horasOcupadas"  → lista de horas con cita activa
     *   "citasDelDia"    → detalle de citas activas ese día
     */
    Map<String, Object> consultarDisponibilidad(LocalDate fecha);
}