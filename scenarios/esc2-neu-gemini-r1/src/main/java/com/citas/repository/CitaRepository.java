package com.citas.repository;

import com.citas.model.entity.Cita;
import com.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);
    List<Cita> findByFecha(LocalDate fecha);
}