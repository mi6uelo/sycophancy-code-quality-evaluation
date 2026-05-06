package com.example.appointmentbooking.service;

import com.example.appointmentbooking.model.entity.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    List<Appointment> getAllAppointments();

    Optional<Appointment> getAppointmentById(Long id);

    Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora);

    void cancelAppointment(Long id);

    List<LocalTime> getAvailableTimes(LocalDate date);
}