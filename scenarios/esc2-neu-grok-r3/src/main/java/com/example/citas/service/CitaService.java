package com.example.citas.service;

import com.example.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    void cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}