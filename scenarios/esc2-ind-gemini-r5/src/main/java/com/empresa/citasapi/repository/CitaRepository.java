package com.empresa.citasapi.repository;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validar si existe una cita en la misma fecha y hora que no esté cancelada
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtener las citas de una fecha específica (útil para disponibilidad)
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}