package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;
import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import com.citasapi.exception.CitaDuplicadaException;
import com.citasapi.exception.CitaNotFoundException;
import com.citasapi.repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // ── Crear cita ───────────────────────────────────────
    @Override
    public CitaResponseDTO crearCita(CitaRequestDTO dto) {

        validarDisponibilidadHorario(dto.getFecha(), dto.getHora(), null);

        Cita cita = new Cita(
            dto.getNombreCliente(),
            dto.getFecha(),
            dto.getHora(),
            dto.getMotivo(),
            EstadoCita.PENDIENTE
        );

        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Listar todas las citas ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(CitaResponseDTO::new)
                .toList();
    }

    // ── Obtener cita por ID ──────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        Cita cita = buscarCitaOFallar(id);
        return new CitaResponseDTO(cita);
    }

    // ── Reagendar cita ───────────────────────────────────
    @Override
    public CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto) {

        Cita cita = buscarCitaOFallar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException(
                "No se puede reagendar una cita que ya fue cancelada.");
        }

        validarDisponibilidadHorario(dto.getNuevaFecha(), dto.getNuevaHora(), id);

        cita.setFecha(dto.getNuevaFecha());
        cita.setHora(dto.getNuevaHora());
        cita.setEstado(EstadoCita.PENDIENTE);

        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Cancelar cita ────────────────────────────────────
    @Override
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOFallar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException(
                "La cita con ID " + id + " ya se encuentra cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Consultar disponibilidad ─────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> consultarDisponibilidad(LocalDate fecha) {
        /*
         * Devuelve las citas activas (no canceladas) de esa fecha.
         * El cliente puede ver qué horas ya están ocupadas y elegir
         * un horario libre.
         */
        return citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(CitaResponseDTO::new)
                .toList();
    }

    // ── Métodos de apoyo ─────────────────────────────────

    private Cita buscarCitaOFallar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    /**
     * Verifica que no exista ya una cita en el mismo slot de fecha/hora.
     *
     * @param excludeId Si se está editando una cita existente, se excluye
     *                  su propio ID de la validación. Null cuando es nueva.
     */
    private void validarDisponibilidadHorario(
            java.time.LocalDate fecha,
            java.time.LocalTime hora,
            Long excludeId) {

        boolean ocupado = (excludeId == null)
            ? citaRepository.existsByFechaAndHora(fecha, hora)
            : citaRepository.existsByFechaAndHoraAndIdNot(fecha, hora, excludeId);

        if (ocupado) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }
}