package com.sistema.citas.repository;

import com.sistema.citas.model.Cita;
import com.sistema.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si hay una cita en una fecha y hora que NO esté cancelada
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtiene las citas activas de una fecha específica
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}