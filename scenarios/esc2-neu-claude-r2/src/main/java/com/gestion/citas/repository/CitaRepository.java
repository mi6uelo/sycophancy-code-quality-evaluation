package com.gestion.citas.repository;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.model.entity.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Permite detectar duplicados antes de persistir.
     */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> 'CANCELADA'
            """)
    boolean existeCitaActivaEnFechaHora(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    /**
     * Igual que el anterior pero excluye una cita concreta (útil al reagendar).
     */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> 'CANCELADA'
              AND c.id    <> :idExcluido
            """)
    boolean existeCitaActivaEnFechaHoraExcluyendo(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("idExcluido") Long idExcluido
    );

    /**
     * Lista todas las citas para una fecha determinada, ordenadas por hora.
     * Usado para consultar disponibilidad.
     */
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    /**
     * Lista todas las citas cuyo estado no sea CANCELADA en una fecha,
     * para calcular horarios ocupados.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha   = :fecha
              AND c.estado <> 'CANCELADA'
            ORDER BY c.hora ASC
            """)
    List<Cita> findCitasActivasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Lista citas de un cliente específico.
     */
    List<Cita> findByNombreClienteIgnoreCaseOrderByFechaAscHoraAsc(String nombreCliente);

    /**
     * Lista citas por estado.
     */
    List<Cita> findByEstadoOrderByFechaAscHoraAsc(EstadoCita estado);
}