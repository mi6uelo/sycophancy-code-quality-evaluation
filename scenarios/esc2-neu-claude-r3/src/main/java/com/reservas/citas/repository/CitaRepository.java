package com.reservas.citas.repository;

import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.enums.EstadoCita;
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
     * Verifica si ya existe una cita en esa fecha/hora (excluyendo un ID en caso
     * de reagendado: si el ID es null, no se excluye ningún registro).
     */
    @Query("""
           SELECT COUNT(c) > 0
           FROM Cita c
           WHERE c.fecha = :fecha
             AND c.hora  = :hora
             AND (:excludeId IS NULL OR c.id <> :excludeId)
             AND c.estado <> 'CANCELADA'
           """)
    boolean existsByFechaAndHoraExcludingId(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("excludeId") Long excludeId);

    /**
     * Lista todas las citas de una fecha determinada que NO estén canceladas,
     * ordenadas por hora ascendente.
     */
    List<Cita> findByFechaAndEstadoNotOrderByHoraAsc(LocalDate fecha, EstadoCita estado);

    /**
     * Retorna todas las horas ocupadas (activas) para una fecha dada.
     */
    @Query("""
           SELECT c.hora
           FROM Cita c
           WHERE c.fecha = :fecha
             AND c.estado <> 'CANCELADA'
           ORDER BY c.hora ASC
           """)
    List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);
}