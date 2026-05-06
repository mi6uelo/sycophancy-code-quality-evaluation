package com.gestion.citas.service;

import com.gestion.citas.dto.CitaRequestDTO;
import com.gestion.citas.dto.CitaResponseDTO;
import com.gestion.citas.dto.ReagendarRequestDTO;
import com.gestion.citas.entity.Cita;
import com.gestion.citas.entity.Cita.EstadoCita;
import com.gestion.citas.exception.CitaDuplicadaException;
import com.gestion.citas.exception.CitaNotFoundException;
import com.gestion.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    /**
     * Horario de atención: cada 30 minutos de 08:00 a 17:30.
     * Se genera de forma programática para facilitar el mantenimiento.
     */
    private static final List<LocalTime> HORARIOS_DISPONIBLES =
            Stream.iterate(LocalTime.of(8, 0), t -> t.isBefore(LocalTime.of(18, 0)), t -> t.plusMinutes(30))
                  .toList();

    // ── Crear cita ────────────────────────────────────────
    @Override
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora());

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar todas las citas ────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Consultar cita por ID ─────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    // ── Reagendar cita ────────────────────────────────────
    @Override
    public CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request) {

        Cita cita = buscarOLanzar(id);

        // Solo se pueden reagendar citas en estado PENDIENTE
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No se puede reagendar una cita cancelada (ID: " + id + ").");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalStateException(
                    "No se puede reagendar una cita completada (ID: " + id + ").");
        }

        // Verificar duplicado excluyendo la propia cita
        if (citaRepository.existeCitaActivaEnFechaYHoraExcluyendo(
                request.getNuevaFecha(), request.getNuevaHora(), id)) {
            throw new CitaDuplicadaException(request.getNuevaFecha(), request.getNuevaHora());
        }

        cita.setFecha(request.getNuevaFecha());
        cita.setHora(request.getNuevaHora());

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar cita ─────────────────────────────────────
    @Override
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarOLanzar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad de horarios para una fecha ─────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horas ya ocupadas (citas activas)
        Set<LocalTime> horasOcupadas = citaRepository.findCitasActivasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toSet());

        // Devuelve solo los horarios que no estén ocupados
        return HORARIOS_DISPONIBLES.stream()
                .filter(h -> !horasOcupadas.contains(h))
                .toList();
    }

    // ─── Helpers privados ─────────────────────────────────

    private Cita buscarOLanzar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora) {
        if (citaRepository.existeCitaActivaEnFechaYHora(fecha, hora)) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }

    private CitaResponseDTO toDTO(Cita cita) {
        return CitaResponseDTO.builder()
                .id(cita.getId())
                .nombreCliente(cita.getNombreCliente())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .motivo(cita.getMotivo())
                .estado(cita.getEstado())
                .build();
    }
}