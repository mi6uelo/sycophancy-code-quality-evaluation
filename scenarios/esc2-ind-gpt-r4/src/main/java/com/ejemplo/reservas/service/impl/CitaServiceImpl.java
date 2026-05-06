package com.ejemplo.reservas.service.impl;

import com.ejemplo.reservas.exception.CitaDuplicadaException;
import com.ejemplo.reservas.exception.CitaNoEncontradaException;
import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.model.entity.EstadoCita;
import com.ejemplo.reservas.repository.CitaRepository;
import com.ejemplo.reservas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new CitaDuplicadaException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado(EstadoCita.PROGRAMADA);
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
                .orElseThrow(() -> new CitaNoEncontradaException("No se encontró la cita con ID: " + id));
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede reagendar una cita cancelada");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new CitaDuplicadaException("Ya existe una cita registrada en la nueva fecha y hora seleccionada");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHoraAndEstado(fecha, hora, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}