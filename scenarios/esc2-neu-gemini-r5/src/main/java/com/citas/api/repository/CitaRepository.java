package com.citas.api.repository;

import com.citas.api.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validación de duplicados
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, String estado);

    // Consulta de disponibilidad
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, String estado);
}