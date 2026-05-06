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
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Excluye el ID indicado, útil al reagendar para no bloquear la cita actual.
     */
    boolean existsByFechaAndHoraAndEstadoNotAndIdNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado,
            Long excludeId
    );

    /**
     * Sobrecarga sin exclusión de ID: usada al crear una cita nueva.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Devuelve todas las citas activas (no canceladas) para una fecha,
     * para calcular la disponibilidad.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}