package com.example.citasapi.service;

import com.example.citasapi.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaService {

    Cita crearCita(Cita cita) throws Exception;

    List<Cita> listarCitas();

    Optional<Cita> consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception;

    Cita cancelarCita(Long id) throws Exception;

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}