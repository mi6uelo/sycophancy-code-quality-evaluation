package com.appointments.service;

import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /**
     * Crea una nueva cita validando que no exista duplicado en fecha/hora.
     */
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO request);

    /**
     * Retorna todas las citas ordenadas por fecha y hora ascendente.
     */
    List<AppointmentResponseDTO> getAllAppointments();

    /**
     * Retorna una cita activa por su ID.
     */
    AppointmentResponseDTO getAppointmentById(Long id);

    /**
     * Reagenda una cita existente a una nueva fecha y hora.
     */
    AppointmentResponseDTO rescheduleAppointment(Long id, RescheduleRequestDTO request);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     */
    AppointmentResponseDTO cancelAppointment(Long id);

    /**
     * Retorna las horas ocupadas y disponibles para una fecha dada.
     */
    AvailabilityResponseDTO checkAvailability(LocalDate fecha);
}