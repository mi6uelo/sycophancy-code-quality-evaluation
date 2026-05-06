package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByFecha(LocalDate fecha);
}