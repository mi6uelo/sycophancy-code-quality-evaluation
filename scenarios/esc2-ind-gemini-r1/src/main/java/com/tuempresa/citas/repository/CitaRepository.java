package com.tuempresa.citas.repository;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si hay una cita en una fecha y hora, ignorando las canceladas
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Busca citas para un día específico (para calcular disponibilidad)
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}