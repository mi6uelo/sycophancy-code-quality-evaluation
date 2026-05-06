package com.example.appointmentmanager.service;

import com.example.appointmentmanager.entity.Appointment;
import com.example.appointmentmanager.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repository;

    public Appointment createAppointment(Appointment appointment) throws Exception {
        if (repository.findByFechaAndHoraAndEstado(appointment.getFecha(), appointment.getHora(), Appointment.Estado.ACTIVA).isPresent()) {
            throw new Exception("Cita duplicada en fecha y hora especificadas.");
        }
        return repository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return repository.findById(id);
    }

    public Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora) throws Exception {
        Optional<Appointment> optAppointment = repository.findById(id);
        if (optAppointment.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Appointment appointment = optAppointment.get();
        if (repository.findByFechaAndHoraAndEstado(newFecha, newHora, Appointment.Estado.ACTIVA).isPresent()) {
            throw new Exception("Nueva fecha y hora ya ocupadas.");
        }
        appointment.setFecha(newFecha);
        appointment.setHora(newHora);
        return repository.save(appointment);
    }

    public void cancelAppointment(Long id) throws Exception {
        Optional<Appointment> optAppointment = repository.findById(id);
        if (optAppointment.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Appointment appointment = optAppointment.get();
        appointment.setEstado(Appointment.Estado.CANCELADA);
        repository.save(appointment);
    }

    public List<LocalTime> getAvailableTimes(LocalDate fecha) {
        List<LocalTime> occupied = repository.findOccupiedTimesByDate(fecha);
        List<LocalTime> allTimes = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
            LocalTime.of(17, 0)
        );
        return allTimes.stream().filter(time -> !occupied.contains(time)).collect(Collectors.toList());
    }
}