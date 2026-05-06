package com.gestion.citas.service.impl;

import com.gestion.citas.exception.CitaDuplicadaException;
import com.gestion.citas.exception.CitaNoEncontradaException;
import com.gestion.citas.exception.EstadoInvalidoException;
import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.model.entity.enums.EstadoCita;
import com.gestion.citas.repository.CitaRepository;
import com.gestion.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    // ─────────────────────────────────────────────────────────────────
    // CREAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        log.info("Intentando crear cita para '{}' el {} a las {}",
                cita.getNombreCliente(), cita.getFecha(), cita.getHora());

        validarSlotDisponible(cita.getFecha(), cita.getHora(), null);

        // Estado inicial siempre PENDIENTE sin importar lo que envíe el cliente
        cita.setEstado(EstadoCita.PENDIENTE);

        Cita citaGuardada = citaRepository.save(cita);
        log.info("Cita creada con ID {}", citaGuardada.getId());
        return citaGuardada;
    }

    // ─────────────────────────────────────────────────────────────────
    // LISTAR TODAS LAS CITAS
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        log.info("Listando todas las citas");
        return citaRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // OBTENER CITA POR ID
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        log.info("Buscando cita con ID {}", id);
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "No se encontró ninguna cita con el ID: " + id));
    }

    // ─────────────────────────────────────────────────────────────────
    // REAGENDAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        log.info("Reagendando cita ID {} → {} {}", id, nuevaFecha, nuevaHora);

        Cita cita = obtenerCitaPorId(id);

        // No se puede reagendar una cita cancelada o completada
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                    "No se puede reagendar una cita que ya fue CANCELADA.");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new EstadoInvalidoException(
                    "No se puede reagendar una cita que ya fue COMPLETADA.");
        }

        // Validar que el nuevo slot no esté ocupado (excluyendo la propia cita)
        validarSlotDisponible(nuevaFecha, nuevaHora, id);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        Cita citaActualizada = citaRepository.save(cita);
        log.info("Cita ID {} reagendada exitosamente", id);
        return citaActualizada;
    }

    // ─────────────────────────────────────────────────────────────────
    // CANCELAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        log.info("Cancelando cita ID {}", id);

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                    "La cita con ID " + id + " ya se encuentra CANCELADA.");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new EstadoInvalidoException(
                    "No se puede cancelar una cita que ya fue COMPLETADA.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        Cita citaCancelada = citaRepository.save(cita);
        log.info("Cita ID {} cancelada exitosamente", id);
        return citaCancelada;
    }

    // ─────────────────────────────────────────────────────────────────
    // CONSULTAR DISPONIBILIDAD
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(LocalDate fecha) {
        log.info("Consultando disponibilidad para la fecha {}", fecha);

        List<Cita> citasActivas = citaRepository.findCitasActivasByFecha(fecha);

        List<String> horasOcupadas = citasActivas.stream()
                .map(c -> c.getHora().toString())
                .toList();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha.toString());
        respuesta.put("totalCitasActivas", citasActivas.size());
        respuesta.put("horasOcupadas", horasOcupadas);
        respuesta.put("citasDelDia", citasActivas);

        return respuesta;
    }

    // ─────────────────────────────────────────────────────────────────
    // MÉTODO PRIVADO: Validar slot disponible
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verifica que no exista ya una cita activa para la fecha y hora indicadas.
     *
     * @param fecha       Fecha a verificar.
     * @param hora        Hora a verificar.
     * @param idExcluido  ID de la cita que debe ignorarse (null si es creación nueva).
     */
    private void validarSlotDisponible(LocalDate fecha, LocalTime hora, Long idExcluido) {
        boolean ocupado;

        if (idExcluido == null) {
            ocupado = citaRepository.existeCitaActivaEnFechaHora(fecha, hora);
        } else {
            ocupado = citaRepository.existeCitaActivaEnFechaHoraExcluyendo(fecha, hora, idExcluido);
        }

        if (ocupado) {
            throw new CitaDuplicadaException(
                    String.format("Ya existe una cita activa para la fecha %s a las %s. "
                                  + "Por favor elija otro horario.", fecha, hora));
        }
    }
}