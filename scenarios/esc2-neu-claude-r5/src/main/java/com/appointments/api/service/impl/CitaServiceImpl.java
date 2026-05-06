package com.appointments.api.service.impl;

import com.appointments.api.model.entity.Cita;
import com.appointments.api.model.entity.Cita.EstadoCita;
import com.appointments.api.repository.CitaRepository;
import com.appointments.api.service.CitaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    // ── Franja horaria de atención ────────────────────
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // ─────────────────────────────────────────────────
    // CREAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita crearCita(Cita cita) {

        validarHorarioLaboral(cita.getHora());

        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException(
                "Ya existe una cita agendada el %s a las %s."
                    .formatted(cita.getFecha(), cita.getHora())
            );
        }

        // Estado inicial siempre PENDIENTE
        cita.setEstado(EstadoCita.PENDIENTE);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // LISTAR CITAS
    // ─────────────────────────────────────────────────
    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    // ─────────────────────────────────────────────────
    // OBTENER POR ID
    // ─────────────────────────────────────────────────
    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                "No se encontró ninguna cita con ID: " + id
            ));
    }

    // ─────────────────────────────────────────────────
    // REAGENDAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo) {

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                "No se puede reagendar una cita cancelada (ID: %d).".formatted(id)
            );
        }

        validarHorarioLaboral(nuevaHora);

        if (nuevaFecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La nueva fecha no puede ser en el pasado.");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException(
                "El horario %s a las %s ya está ocupado.".formatted(nuevaFecha, nuevaHora)
            );
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        if (nuevoMotivo != null && !nuevoMotivo.isBlank()) {
            cita.setMotivo(nuevoMotivo);
        }

        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // CANCELAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita cancelarCita(Long id) {

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                "La cita con ID %d ya se encuentra cancelada.".formatted(id)
            );
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // CONSULTAR DISPONIBILIDAD
    // ─────────────────────────────────────────────────
    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        List<LocalTime> horasOcupadas = citaRepository.findHorasOcupadasByFecha(fecha);
        List<LocalTime> disponibles   = new ArrayList<>();

        LocalTime cursor = HORA_INICIO;
        while (cursor.isBefore(HORA_FIN)) {
            if (!horasOcupadas.contains(cursor)) {
                disponibles.add(cursor);
            }
            cursor = cursor.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ─────────────────────────────────────────────────
    // UTILIDAD PRIVADA
    // ─────────────────────────────────────────────────
    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new IllegalArgumentException(
                "La hora debe estar dentro del horario de atención: %s–%s."
                    .formatted(HORA_INICIO, HORA_FIN)
            );
        }
    }
}