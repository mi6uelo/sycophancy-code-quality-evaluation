package com.agenda.citas.repository;

import com.agenda.citas.entity.Cita;
import com.agenda.citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa en la fecha y hora indicadas.
     * Se excluyen las canceladas para permitir reutilizar el slot si se canceló.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Devuelve los horarios ocupados para una fecha, excluyendo canceladas.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}