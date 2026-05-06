package com.reservas.citas.service.impl;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNoEncontradaException;
import com.reservas.citas.exception.EstadoInvalidoException;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.entity.EstadoCita;
import com.reservas.citas.repository.CitaRepository;
import com.reservas.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
* Implementación de CitaService con lógica de negocio y validaciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitaServiceImpl implements CitaService {

    /* ── Horario de atención ──────────────────────────────────────────────── */
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(17, 0);
    private static final int       BLOQUE_MINS = 30;

    private final CitaRepository citaRepository;

    /* ── Crear ────────────���───────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO dto) {
        log.info("Creando cita para '{}' el {} a las {}", dto.nombreCliente(), dto.fecha(), dto.hora());

        validarHorarioLaboral(dto.hora());
        validarDisponibilidad(dto.fecha(), dto.hora(), null);

        Cita cita = Cita.builder()
            .nombreCliente(dto.nombreCliente())
            .fecha(dto.fecha())
            .hora(dto.hora())
            .motivo(dto.motivo())
            .estado(EstadoCita.PENDIENTE)
            .build();

        return toDTO(citaRepository.save(cita));
    }

    /* ── Listar ───────────────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        log.info("Listando todas las citas");
        return citaRepository.findAll()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    /* ── Obtener por ID ───────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        log.info("Consultando cita con id={}", id);
        return toDTO(buscarOLanzarError(id));
    }

    /* ── Reagendar ────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, CitaRequestDTO dto) {
        log.info("Reagendando cita id={} a {} {}", id, dto.fecha(), dto.hora());

        Cita cita = buscarOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                "No es posible reagendar una cita que ya fue cancelada. ID: " + id
            );
        }

        validarHorarioLaboral(dto.hora());

        boolean mismoHorario = cita.getFecha().equals(dto.fecha())
                            && cita.getHora().equals(dto.hora());
        if (!mismoHorario) {
            validarDisponibilidad(dto.fecha(), dto.hora(), id);
        }

        cita.setNombreCliente(dto.nombreCliente());
        cita.setFecha(dto.fecha());
        cita.setHora(dto.hora());
        cita.setMotivo(dto.motivo());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    /* ── Cancelar ─────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {
        log.info("Cancelando cita id={}", id);

        Cita cita = buscarOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                "La cita con ID " + id + " ya se encuentra cancelada."
            );
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    /* ── Disponibilidad ───────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO consultarDisponibilidad(LocalDate fecha) {
        log.info("Consultando disponibilidad para {}", fecha);

        List<LocalTime> ocupadas = citaRepository
            .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
            .stream()
            .map(Cita::getHora)
            .toList();

        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime cursor = HORA_INICIO;

        while (cursor.isBefore(HORA_FIN)) {
            if (!ocupadas.contains(cursor)) {
                disponibles.add(cursor);
            }
            cursor = cursor.plusMinutes(BLOQUE_MINS);
        }

        return new DisponibilidadResponseDTO(fecha, ocupadas, disponibles);
    }

    /* ── Helpers privados ─────────────────────────────────────────────────── */

    /**
     * Busca una cita por ID o lanza CitaNoEncontradaException.
     */
    private Cita buscarOLanzarError(Long id) {
        return citaRepository.findById(id)
            .orElseThrow(() ->
                new CitaNoEncontradaException("No existe ninguna cita con ID: " + id)
            );
    }

    /**
     * Verifica que la hora solicitada esté dentro del horario de atención
     * y que coincida con un bloque válido de BLOQUE_MINS minutos.
     */
    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new EstadoInvalidoException(
                "La hora debe estar dentro del horario de atención: "
                + HORA_INICIO + " – " + HORA_FIN + "."
            );
        }
        if (hora.getMinute() % BLOQUE_MINS != 0) {
            throw new EstadoInvalidoException(
                "Las citas deben agendarse en bloques de " + BLOQUE_MINS
                + " minutos (ej. 08:00, 08:30, 09:00)."
            );
        }
    }

    /**
     * Valida que no exista otra cita activa en la misma fecha/hora.
     * El parámetro idExcluir permite ignorar la propia cita al reagendar.
     */
    private void validarDisponibilidad(LocalDate fecha, LocalTime hora, Long idExcluir) {
        citaRepository.findByFechaAndHora(fecha, hora).ifPresent(existente -> {
            boolean esMisma   = existente.getId().equals(idExcluir);
            boolean cancelada = existente.getEstado() == EstadoCita.CANCELADA;

            if (!esMisma && !cancelada) {
                throw new CitaDuplicadaException(
                    "Ya existe una cita activa el " + fecha + " a las " + hora + "."
                );
            }
        });
    }

    /**
     * Convierte una entidad Cita en su DTO de respuesta.
     */
    private CitaResponseDTO toDTO(Cita cita) {
        return new CitaResponseDTO(
            cita.getId(),
            cita.getNombreCliente(),
            cita.getFecha(),
            cita.getHora(),
            cita.getMotivo(),
            cita.getEstado()
        );
    }
}
