package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
import com.example.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    List<Cita> findByFecha(LocalDate fecha);
}