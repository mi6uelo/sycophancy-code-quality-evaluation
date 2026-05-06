package com.example.citasapi.repository;

import com.example.citasapi.model.entity.Cita;
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

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado != 'CANCELADA'")
    List<Cita> findOcupadasByFecha(@Param("fecha") LocalDate fecha);
}