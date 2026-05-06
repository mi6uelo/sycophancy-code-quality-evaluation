package com.gestion.citas.repository;

import com.gestion.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Para validar duplicados (excluyendo citas canceladas si fuera necesario)
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, String estado);

    // Para consultar disponibilidad
    List<Cita> findByFecha(LocalDate fecha);
}