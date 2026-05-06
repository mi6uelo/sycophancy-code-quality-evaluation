package com.agenda.citas.service;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;
import com.agenda.citas.entity.Cita;
import com.agenda.citas.enums.EstadoCita;
import com.agenda.citas.exception.CitaDuplicadaException;
import com.agenda.citas.exception.CitaNotFoundException;
import com.agenda.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    /*
     * Horario de atención: 08:00 – 17:00, franjas de 1 hora.
     * Ajusta el rango según las necesidades del negocio.
     */
    private static final LocalTime HORA_INICIO   = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN      = LocalTime.of(17, 0);

    // ──────────────────────────────────────────────────────────────────────────
    // Crear cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora(), null);

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .build();

        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Listar citas
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Consultar por ID
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO consultarCitaPorId(Long id) {
        return mapToResponse(obtenerCitaOLanzarExcepcion(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Reagendar cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {

        Cita cita = obtenerCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita cancelada (ID: " + id + ").");
        }

        /*
         * Si la nueva fecha/hora coincide con la actual de la misma cita,
         * no se considera duplicado (se pasa el id actual para excluirlo
         * de la validación dentro del helper).
         */
        boolean mismoSlot = cita.getFecha().equals(nuevaFecha)
                            && cita.getHora().equals(nuevaHora);

        if (!mismoSlot) {
            validarDisponibilidadHorario(nuevaFecha, nuevaHora, id);
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cancelar cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = obtenerCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Consultar disponibilidad de horario
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horarios ocupados (se excluyen citas canceladas)
        List<LocalTime> ocupados = citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .toList();

        // Genera la lista de todos los slots del día y filtra los libres
        List<LocalTime> disponibles = new java.util.ArrayList<>();
        LocalTime slot = HORA_INICIO;

        while (slot.isBefore(HORA_FIN)) {
            if (!ocupados.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusHours(1);
        }

        return disponibles;
    }

    // ────────────────────────────────────────────────────────��─────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Verifica que el slot fecha/hora no esté ocupado por otra cita activa.
     *
     * @param excludeId ID de la cita a excluir de la verificación (puede ser null).
     */
    private void validarDisponibilidadHorario(
            LocalDate fecha, LocalTime hora, Long excludeId) {

        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);

        if (ocupado) {
            /*
             * Si el slot está ocupado pero es por la misma cita que queremos
             * reagendar, lo permitimos. Para los demás casos lanzamos excepción.
             */
            if (excludeId != null) {
                Cita existente = citaRepository
                        .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                        .stream()
                        .filter(c -> c.getFecha().equals(fecha)
                                     && c.getHora().equals(hora)
                                     && c.getId().equals(excludeId))
                        .findFirst()
                        .orElse(null);

                if (existente == null) {
                    // El slot lo ocupa OTRA cita → conflicto real
                    throw new CitaDuplicadaException(fecha, hora);
                }
            } else {
                throw new CitaDuplicadaException(fecha, hora);
            }
        }
    }

    private Cita obtenerCitaOLanzarExcepcion(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    private CitaResponseDTO mapToResponse(Cita cita) {
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