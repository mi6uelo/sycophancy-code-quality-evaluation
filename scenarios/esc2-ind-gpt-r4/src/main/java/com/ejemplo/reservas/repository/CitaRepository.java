package com.ejemplo.reservas.repository;

import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    boolean existsByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}