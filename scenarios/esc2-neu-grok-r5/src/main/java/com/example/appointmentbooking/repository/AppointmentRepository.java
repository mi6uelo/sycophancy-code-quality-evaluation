package com.example.appointmentbooking.repository;

import com.example.appointmentbooking.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Appointment> findByFecha(LocalDate fecha);

    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha AND a.estado <> 'CANCELLED'")
    List<Appointment> findActiveAppointmentsByFecha(@Param("fecha") LocalDate fecha);
}