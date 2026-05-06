package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;
import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import com.citasapi.exception.CitaDuplicadaException;
import com.citasapi.exception.CitaNotFoundException;
import com.citasapi.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    // Horario de atención: 08:00 a 17:30, intervalos de 30 minutos
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 30);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    // ── Crear ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        verificarDisponibilidadParaNuevaCita(request.getFecha(), request.getHora());

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PROGRAMADA)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Obtener por ID ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarError(id));
    }

    // ── Reagendar ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request) {

        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita cancelada (ID: " + id + ").");
        }

        verificarDisponibilidadParaReagendar(
                request.getNuevaFecha(), request.getNuevaHora(), id);

        cita.setFecha(request.getNuevaFecha());
        cita.setHora(request.getNuevaHora());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horas ocupadas ese día (excluye canceladas)
        List<LocalTime> ocupadas = citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        // Generar todos los slots del día y filtrar los libres
        List<LocalTime> disponibles = new java.util.ArrayList<>();
        LocalTime slot = HORA_INICIO;
        while (!slot.isAfter(HORA_FIN)) {
            if (!ocupadas.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ── Utilidades privadas ──────────────────────────────────────────────────

    private Cita buscarCitaOLanzarError(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() ->
                        new CitaNotFoundException("Cita no encontrada con ID: " + id));
    }

    /**
     * Validación al CREAR: no debe existir ninguna cita activa en esa fecha/hora.
     */
    private void verificarDisponibilidadParaNuevaCita(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new CitaDuplicadaException(
                    "Ya existe una cita programada para la fecha "
                    + fecha + " a las " + hora + ".");
        }
    }

    /**
     * Validación al REAGENDAR: igual que la anterior, pero excluye la propia cita
     * para evitar que se bloquee a sí misma si la fecha/hora no cambia.
     */
    private void verificarDisponibilidadParaReagendar(
            LocalDate fecha, LocalTime hora, Long excludeId) {

        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNotAndIdNot(
                        fecha, hora, EstadoCita.CANCELADA, excludeId);
        if (ocupado) {
            throw new CitaDuplicadaException(
                    "Ya existe una cita programada para la fecha "
                    + fecha + " a las " + hora + ".");
        }
    }

    /** Convierte una entidad Cita a su DTO de respuesta. */
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