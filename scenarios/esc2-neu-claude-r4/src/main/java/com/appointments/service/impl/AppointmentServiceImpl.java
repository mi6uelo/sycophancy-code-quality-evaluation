package com.appointments.service.impl;

import com.appointments.exception.AppointmentCancelledException;
import com.appointments.exception.AppointmentNotFoundException;
import com.appointments.exception.DuplicateAppointmentException;
import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;
import com.appointments.model.entity.Appointment;
import com.appointments.model.entity.enums.AppointmentStatus;
import com.appointments.repository.AppointmentRepository;
import com.appointments.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    // Horario de atención: 08:00 – 17:00, intervalos de 30 minutos
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME  = LocalTime.of(17, 0);
    private static final int       SLOT_MINUTES  = 30;

    private final AppointmentRepository appointmentRepository;

    // ── Crear ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {

        log.info("Creando cita para {} el {} a las {}",
                request.nombreCliente(), request.fecha(), request.hora());

        if (appointmentRepository.existsByFechaAndHora(request.fecha(), request.hora())) {
            throw new DuplicateAppointmentException(request.fecha(), request.hora());
        }

        Appointment appointment = Appointment.builder()
                .nombreCliente(request.nombreCliente())
                .fecha(request.fecha())
                .hora(request.hora())
                .motivo(request.motivo())
                .estado(AppointmentStatus.PENDIENTE)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Cita creada con ID {}", saved.getId());

        return toResponseDTO(saved);
    }

    // ── Listar todas ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository
                .findAllByOrderByFechaAscHoraAsc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ── Consultar por ID ──────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return toResponseDTO(findActiveOrThrow(id));
    }

    // ── Reagendar ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(Long id,
                                                        RescheduleRequestDTO request) {

        Appointment appointment = findActiveOrThrow(id);

        log.info("Reagendando cita ID {} a {} {}",
                id, request.nuevaFecha(), request.nuevaHora());

        // Verificar que la nueva fecha/hora no esté ocupada por OTRA cita
        if (appointmentRepository.existsByFechaAndHoraAndIdNot(
                request.nuevaFecha(), request.nuevaHora(), id)) {
            throw new DuplicateAppointmentException(request.nuevaFecha(), request.nuevaHora());
        }

        appointment.setFecha(request.nuevaFecha());
        appointment.setHora(request.nuevaHora());
        appointment.setEstado(AppointmentStatus.REAGENDADA);

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long id) {

        // Para cancelar necesitamos el registro aunque ya esté cancelado (idempotente info)
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getEstado() == AppointmentStatus.CANCELADA) {
            throw new AppointmentCancelledException(id);
        }

        log.info("Cancelando cita ID {}", id);
        appointment.setEstado(AppointmentStatus.CANCELADA);

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // ── Disponibilidad ────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDTO checkAvailability(LocalDate fecha) {

        List<LocalTime> horasOcupadas = appointmentRepository
                .findActiveByFecha(fecha, AppointmentStatus.CANCELADA)
                .stream()
                .map(Appointment::getHora)
                .toList();

        List<LocalTime> todosLosSlots = buildTimeSlots();
        List<LocalTime> horasDisponibles = todosLosSlots.stream()
                .filter(slot -> !horasOcupadas.contains(slot))
                .toList();

        return new AvailabilityResponseDTO(
                fecha,
                horasOcupadas,
                horasDisponibles,
                horasOcupadas.size(),
                horasDisponibles.size()
        );
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Busca una cita activa (no cancelada) o lanza excepción.
     */
    private Appointment findActiveOrThrow(Long id) {
        return appointmentRepository
                .findActiveById(id, AppointmentStatus.CANCELADA)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    /**
     * Genera todos los slots de tiempo dentro del horario de atención.
     */
    private List<LocalTime> buildTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = OPENING_TIME;
        while (!current.isAfter(CLOSING_TIME.minusMinutes(SLOT_MINUTES))) {
            slots.add(current);
            current = current.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }

    /**
     * Convierte una entidad Appointment en su DTO de respuesta.
     */
    private AppointmentResponseDTO toResponseDTO(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getNombreCliente(),
                a.getFecha(),
                a.getHora(),
                a.getMotivo(),
                a.getEstado()
        );
    }
}