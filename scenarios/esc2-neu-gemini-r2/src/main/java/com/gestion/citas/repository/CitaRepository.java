package com.gestion.citas.repository;

import com.gestion.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validación de duplicados
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Consulta de disponibilidad
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, Cita.EstadoCita estado);
}