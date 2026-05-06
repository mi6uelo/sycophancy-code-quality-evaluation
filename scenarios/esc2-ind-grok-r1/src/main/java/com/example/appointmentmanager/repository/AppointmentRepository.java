package com.example.appointmentmanager.repository;

import com.example.appointmentmanager.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, Appointment.Estado estado);

    @Query("SELECT a.hora FROM Appointment a WHERE a.fecha = :fecha AND a.estado = 'ACTIVA'")
    List<LocalTime> findOccupiedTimesByDate(@Param("fecha") LocalDate fecha);
}