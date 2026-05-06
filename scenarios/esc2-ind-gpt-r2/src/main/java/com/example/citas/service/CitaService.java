package com.example.citas.service;

import com.example.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita consultarPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    Cita cancelarCita(Long id);

    boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora);
}