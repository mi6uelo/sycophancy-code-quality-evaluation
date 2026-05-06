package com.gestion.citas.service;

import com.gestion.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ICitaService {
    Cita crearCita(Cita cita);
    List<Cita> listarTodas();
    Cita obtenerPorId(Long id);
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    void cancelarCita(Long id);
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}