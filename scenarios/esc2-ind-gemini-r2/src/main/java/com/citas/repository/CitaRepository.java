package com.citas.repository;

import com.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, Cita.EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}