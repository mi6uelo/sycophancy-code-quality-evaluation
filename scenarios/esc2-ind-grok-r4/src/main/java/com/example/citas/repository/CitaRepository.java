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

    Optional<Cita> findByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, String estado);

    List<Cita> findByFechaAndEstado(LocalDate fecha, String estado);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado = 'activa'")
    List<Cita> findActiveByFecha(@Param("fecha") LocalDate fecha);
}