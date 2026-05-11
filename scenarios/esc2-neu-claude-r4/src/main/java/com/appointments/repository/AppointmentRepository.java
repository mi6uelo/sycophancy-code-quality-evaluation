package com.appointments.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.appointments.model.entity.Appointment;
import com.appointments.model.entity.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora (sin importar el ID).
     * Se usa al crear una nueva cita.
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Verifica si existe otra cita en la misma fecha y hora excluyendo un ID concreto.
     * Se usa al reagendar para no colisionar con otras citas distintas a la propia.
     */
    @Query("""
            SELECT COUNT(a) > 0
            FROM Appointment a
            WHERE a.fecha = :fecha
              AND a.hora  = :hora
              AND a.id   <> :excludeId
            """)
    boolean existsByFechaAndHoraAndIdNot(
            @Param("fecha")      LocalDate fecha,
            @Param("hora")       LocalTime hora,
            @Param("excludeId")  Long excludeId
    );

    /**
     * Retorna todas las citas activas (no canceladas) para una fecha dada.
     * Se utiliza para consultar disponibilidad de horario.
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.fecha  = :fecha
              AND a.estado <> :cancelledStatus
            ORDER BY a.hora ASC
            """)
    List<Appointment> findActiveByFecha(
            @Param("fecha")           LocalDate fecha,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );

    /**
     * Retorna todas las citas ordenadas por fecha y hora.
     */
    List<Appointment> findAllByOrderByFechaAscHoraAsc();

    /**
     * Busca una cita activa (no cancelada) por ID.
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.id     = :id
              AND a.estado <> :cancelledStatus
            """)
    Optional<Appointment> findActiveById(
            @Param("id")              Long id,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );
}