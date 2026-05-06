package com.example.citasapp.repository;

import com.example.citasapp.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByFecha(LocalDate fecha);

    Optional<Cita> findByIdAndEstadoNot(Long id, Cita.EstadoCita estado);
}