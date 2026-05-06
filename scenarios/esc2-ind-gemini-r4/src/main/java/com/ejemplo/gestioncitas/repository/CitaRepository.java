package com.ejemplo.gestioncitas.repository;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si existe una cita en esa fecha y hora, excluyendo las canceladas
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtiene todas las citas de una fecha específica, excluyendo las canceladas
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}