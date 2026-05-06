package com.example.citas.repository;

import com.example.citas.entity.Cita;
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

    // Buscar cita por fecha y hora para validar duplicados
    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Listar citas por fecha (para disponibilidad)
    List<Cita> findByFecha(LocalDate fecha);

    // Buscar citas por estado
    List<Cita> findByEstado(Cita.EstadoCita estado);
}