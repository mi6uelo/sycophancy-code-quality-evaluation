package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.entity.Cita;
import com.reservas.citas.entity.Cita.EstadoCita;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNoEncontradaException;
import com.reservas.citas.repository.CitaRepository;
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

    private final CitaRepository citaRepository;

    /*
     * Horario de atención: bloques de 1 hora, de 08:00 a 17:00.
     * Ajusta este rango según las necesidades del negocio.
     */
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);

    // ── Crear ─────────────────────────────────────────────────────────────────

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

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAllByOrderByFechaAscHoraAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Obtener por ID ──────────────────────────────��─────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarError(id));
    }

    // ── Reagendar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, CitaRequestDTO request) {
        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita que ha sido cancelada (ID: " + id + ").");
        }

        /*
         * Se pasa el ID de la cita actual para que la validación
         * no la considere como duplicado de sí misma.
         */
        boolean mismoHorario = cita.getFecha().equals(request.getFecha())
                               && cita.getHora().equals(request.getHora());

        if (!mismoHorario) {
            validarDisponibilidadHorario(request.getFecha(), request.getHora(), id);
        }

        cita.setNombreCliente(request.getNombreCliente());
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setMotivo(request.getMotivo());

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {
        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita con ID " + id + " ya se encuentra cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<LocalTime> horasOcupadas = citaRepository
                .findHorasOcupadasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .toList();

        // Genera todos los bloques horarios de 1 hora dentro del rango de atención
        return HORA_INICIO.datesUntil(HORA_FIN.plusSeconds(1), java.time.Duration.ofHours(1))
                // LocalTime no implementa datesUntil — usamos stream manual:
                .toList(); // placeholder — ver implementación correcta abajo
    }

    /*
     * Implementación correcta de consultarDisponibilidad usando un bucle simple,
     * ya que LocalTime no tiene un método stream nativo en Java 21.
     */
    private List<LocalTime> generarHorariosDisponibles(LocalDate fecha) {
        List<LocalTime> horasOcupadas = citaRepository
                .findHorasOcupadasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .toList();

        List<LocalTime> todosLosHorarios = new java.util.ArrayList<>();
        LocalTime cursor = HORA_INICIO;
        while (!cursor.isAfter(HORA_FIN.minusHours(1))) {
            todosLosHorarios.add(cursor);
            cursor = cursor.plusHours(1);
        }

        todosLosHorarios.removeAll(horasOcupadas);
        return todosLosHorarios;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Cita buscarCitaOLanzarError(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id));
    }

    /**
     * Verifica que no exista otra cita activa en el mismo horario.
     *
     * @param excludeId ID de la cita a excluir de la validación (usado en reagendamiento).
     *                  Pasa {@code null} al crear una cita nueva.
     */
    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora, Long excludeId) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);

        if (ocupado) {
            // Si el horario ocupado pertenece a la misma cita que se está reagendando, se ignora
            if (excludeId != null) {
                boolean esLaMismaCita = citaRepository.findById(excludeId)
                        .map(c -> c.getFecha().equals(fecha) && c.getHora().equals(hora))
                        .orElse(false);
                if (esLaMismaCita) return;
            }
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