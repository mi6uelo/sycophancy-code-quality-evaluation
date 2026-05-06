package com.example.appointmentbooking.service.impl;

import com.example.appointmentbooking.model.entity.Appointment;
import com.example.appointmentbooking.repository.AppointmentRepository;
import com.example.appointmentbooking.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    private static final int SLOT_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // Verificar si ya existe una cita en la misma fecha y hora
        Optional<Appointment> existing = appointmentRepository.findByFechaAndHora(appointment.getFecha(), appointment.getHora());
        if (existing.isPresent() && !existing.get().getEstado().equals(Appointment.Estado.CANCELLED)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }

        // Validar que la hora esté dentro del horario de trabajo
        if (appointment.getHora().isBefore(START_TIME) || appointment.getHora().isAfter(END_TIME.minusMinutes(SLOT_DURATION_MINUTES))) {
            throw new RuntimeException("La hora debe estar entre 09:00 y 16:30.");
        }

        appointment.setEstado(Appointment.Estado.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Cita no encontrada.");
        }

        Appointment appointment = optionalAppointment.get();

        // Verificar que no haya conflicto con la nueva fecha y hora
        Optional<Appointment> conflict = appointmentRepository.findByFechaAndHora(newFecha, newHora);
        if (conflict.isPresent() && !conflict.get().getId().equals(id) && !conflict.get().getEstado().equals(Appointment.Estado.CANCELLED)) {
            throw new RuntimeException("Ya existe una cita programada para la nueva fecha y hora.");
        }

        // Validar nueva hora
        if (newHora.isBefore(START_TIME) || newHora.isAfter(END_TIME.minusMinutes(SLOT_DURATION_MINUTES))) {
            throw new RuntimeException("La nueva hora debe estar entre 09:00 y 16:30.");
        }

        appointment.setFecha(newFecha);
        appointment.setHora(newHora);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Cita no encontrada.");
        }

        Appointment appointment = optionalAppointment.get();
        appointment.setEstado(Appointment.Estado.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    public List<LocalTime> getAvailableTimes(LocalDate date) {
        List<Appointment> activeAppointments = appointmentRepository.findActiveAppointmentsByFecha(date);
        List<LocalTime> availableTimes = new ArrayList<>();

        LocalTime current = START_TIME;
        while (current.isBefore(END_TIME)) {
            boolean isTaken = activeAppointments.stream()
                    .anyMatch(app -> app.getHora().equals(current));
            if (!isTaken) {
                availableTimes.add(current);
            }
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return availableTimes;
    }
}