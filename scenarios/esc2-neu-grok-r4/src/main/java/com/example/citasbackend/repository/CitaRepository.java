package com.example.citasbackend.repository;

import com.example.citasbackend.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByEstado(Cita.EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}