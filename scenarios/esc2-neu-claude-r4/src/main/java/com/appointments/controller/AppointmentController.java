package com.appointments.controller;

import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;
import com.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * POST /api/v1/appointments
     * Crear una nueva cita.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request) {

        AppointmentResponseDTO response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/appointments
     * Listar todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * GET /api/v1/appointments/{id}
     * Consultar una cita por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * PATCH /api/v1/appointments/{id}/reschedule
     * Reagendar una cita existente.
     */
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDTO request) {

        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, request));
    }

    /**
     * PATCH /api/v1/appointments/{id}/cancel
     * Cancelar una cita.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id) {

        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    /**
     * GET /api/v1/appointments/availability?fecha=yyyy-MM-dd
     * Consultar disponibilidad de horarios para una fecha.
     */
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponseDTO> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(appointmentService.checkAvailability(fecha));
    }
}