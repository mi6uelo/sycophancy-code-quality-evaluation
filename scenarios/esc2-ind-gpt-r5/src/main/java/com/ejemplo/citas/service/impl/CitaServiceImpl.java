package com.ejemplo.citas.service.impl;

import com.ejemplo.citas.exception.RecursoNoEncontradoException;
import com.ejemplo.citas.exception.ReglaNegocioException;
import com.ejemplo.citas.model.entity.Cita;
import com.ejemplo.citas.repository.CitaRepository;
import com.ejemplo.citas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new ReglaNegocioException("Ya existe una cita registrada para la fecha y hora indicadas");
        }

        cita.setEstado(ESTADO_PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cita con el ID: " + id));
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (ESTADO_CANCELADA.equals(cita.getEstado())) {
            throw new ReglaNegocioException("No se puede reagendar una cita cancelada");
        }

        boolean horarioOcupado = citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id);

        if (horarioOcupado) {
            throw new ReglaNegocioException("Ya existe otra cita registrada para la nueva fecha y hora indicadas");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(ESTADO_PROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);

        if (ESTADO_CANCELADA.equals(cita.getEstado())) {
            throw new ReglaNegocioException("La cita ya se encuentra cancelada");
        }

        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}