package com.example.appointmentmanager.controller;

import com.example.appointmentmanager.entity.Appointment;
import com.example.appointmentmanager.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        try {
            Appointment saved = service.createAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return service.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = service.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(@PathVariable Long id, @RequestParam LocalDate fecha, @RequestParam LocalTime hora) {
        try {
            Appointment updated = service.rescheduleAppointment(id, fecha, hora);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        try {
            service.cancelAppointment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/availability")
    public List<LocalTime> getAvailableTimes(@RequestParam LocalDate fecha) {
        return service.getAvailableTimes(fecha);
    }
}