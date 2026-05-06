package com.gestion.citas.repository;

import com.gestion.citas.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa (no cancelada) en la fecha y hora indicadas.
     * Se usa para prevenir duplicados antes de crear o reagendar.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
            """)
    boolean existeCitaActivaEnFechaYHora(
            @Param("fecha") LocalDate fecha,
            @Param("hora")  LocalTime hora
    );

    /**
     * Igual que el anterior pero excluyendo un ID específico.
     * Útil al reagendar para no comparar la cita consigo misma.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
              AND c.id    <> :idExcluir
            """)
    boolean existeCitaActivaEnFechaYHoraExcluyendo(
            @Param("fecha")      LocalDate fecha,
            @Param("hora")       LocalTime hora,
            @Param("idExcluir")  Long idExcluir
    );

    /**
     * Devuelve todas las citas activas (no canceladas) de una fecha concreta.
     * Se utiliza para consultar la disponibilidad de horarios.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha  = :fecha
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
            ORDER BY c.hora
            """)
    List<Cita> findCitasActivasByFecha(@Param("fecha") LocalDate fecha);
}