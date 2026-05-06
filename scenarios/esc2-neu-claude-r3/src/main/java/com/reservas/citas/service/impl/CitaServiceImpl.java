package com.reservas.citas.service.impl;

import com.reservas.citas.exception.CitaCanceladaException;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNotFoundException;
import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.enums.EstadoCita;
import com.reservas.citas.repository.CitaRepository;
import com.reservas.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private static final LocalTime HORA_INICIO  = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Crear
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora(), null);

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PROGRAMADA)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Obtener por ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarExcepcion(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reagendar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto) {

        Cita cita = buscarCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaCanceladaException(id);
        }

        validarDisponibilidadHorario(dto.getNuevaFecha(), dto.getNuevaHora(), id);

        cita.setFecha(dto.getNuevaFecha());
        cita.setHora(dto.getNuevaHora());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancelar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaCanceladaException(id);
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Disponibilidad
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        List<LocalTime> horasOcupadas = citaRepository.findHorasOcupadasByFecha(fecha);
        List<LocalTime> disponibles   = new ArrayList<>();

        LocalTime slot = HORA_INICIO;
        while (!slot.isAfter(HORA_FIN.minusMinutes(INTERVALO_MIN))) {
            if (!horasOcupadas.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private Cita buscarCitaOLanzarExcepcion(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    /**
     * Verifica que el slot fecha/hora no esté ocupado por otra cita activa.
     *
     * @param excludeId ID de la cita que se está reagendando (null si es nueva).
     */
    private void validarDisponibilidadHorario(LocalDate fecha,
                                               LocalTime hora,
                                               Long excludeId) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraExcludingId(fecha, hora, excludeId);
        if (ocupado) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }

    /** Convierte una entidad Cita en su DTO de respuesta. */
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