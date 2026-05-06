package com.citasapi.repository;

import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora.
     * Se usa para prevenir duplicados al crear o reagendar.
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Igual al anterior pero excluyendo un ID específico.
     * Útil al reagendar para no colisionar con la misma cita.
     */
    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    /**
     * Devuelve todas las citas de una fecha concreta que NO estén canceladas,
     * para calcular la disponibilidad del horario.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}