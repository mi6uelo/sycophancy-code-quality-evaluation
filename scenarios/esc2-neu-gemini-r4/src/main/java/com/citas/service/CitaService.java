package com.citas.service;

import com.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {
    Cita crearCita(Cita cita);
    List<Cita> listarTodas();
    Cita obtenerPorId(Long id);
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    void cancelarCita(Long id);
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}