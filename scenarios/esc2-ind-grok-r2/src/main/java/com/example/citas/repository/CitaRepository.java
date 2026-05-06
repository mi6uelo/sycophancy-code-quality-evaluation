package com.example.citas.repository;

import com.example.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaAndHora(LocalDate fecha, java.time.LocalTime hora);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado != 'CANCELADA'")
    List<Cita> findCitasOcupadasByFecha(@Param("fecha") LocalDate fecha);
}