package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
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

    // Verificar si existe una cita activa en la misma fecha y hora
    boolean existsByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, String estado);

    // Listar citas por fecha
    List<Cita> findByFecha(LocalDate fecha);

    // Encontrar cita por ID y estado activa
    Optional<Cita> findByIdAndEstado(Long id, String estado);
}