package com.reservas.citas.repository;

import com.reservas.citas.entity.Cita;
import com.reservas.citas.entity.Cita.EstadoCita;
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
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Se excluye el estado CANCELADA para permitir reutilizar ese horario.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Retorna todas las citas activas (no canceladas) para una fecha específica,
     * ordenadas por hora ascendente. Útil para consultar disponibilidad.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha = :fecha
              AND c.estado <> 'CANCELADA'
            ORDER BY c.hora ASC
            """)
    List<Cita> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Lista todas las citas ordenadas por fecha y hora.
     */
    List<Cita> findAllByOrderByFechaAscHoraAsc();
}