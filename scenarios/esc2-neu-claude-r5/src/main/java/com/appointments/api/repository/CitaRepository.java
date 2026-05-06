package com.appointments.api.repository;

import com.appointments.api.model.entity.Cita;
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
     * Verifica si ya existe una cita en la misma fecha y hora (control de duplicados).
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Verifica duplicado excluyendo la cita con el ID indicado (usado al reagendar).
     */
    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    /**
     * Devuelve todas las citas para una fecha concreta, ordenadas por hora.
     */
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    /**
     * Devuelve las horas ya ocupadas en una fecha, sin importar el estado.
     */
    @Query("SELECT c.hora FROM Cita c WHERE c.fecha = :fecha AND c.estado <> 'CANCELADA'")
    List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Busca citas activas (no canceladas) de un cliente específico.
     */
    List<Cita> findByNombreClienteIgnoreCaseAndEstadoNot(
            String nombreCliente, Cita.EstadoCita estado);
}