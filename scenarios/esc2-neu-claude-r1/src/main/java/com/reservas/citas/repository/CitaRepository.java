package com.reservas.citas.repository;

import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
* Repositorio JPA para la entidad Cita.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
        LocalDate fecha,
        LocalTime hora,
        EstadoCita estado
    );

    /**
     * Devuelve todas las citas de una fecha específica que no estén canceladas.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);

    /**
     * Busca una cita por fecha y hora (para validar duplicados al reagendar).
     */
    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Lista todas las citas cuyo estado sea el indicado.
     */
    List<Cita> findByEstado(EstadoCita estado);
}
