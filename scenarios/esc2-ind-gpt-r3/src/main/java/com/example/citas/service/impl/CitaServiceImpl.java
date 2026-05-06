package com.example.citas.service.impl;

import com.example.citas.exception.ResourceNotFoundException;
import com.example.citas.model.entity.Cita;
import com.example.citas.model.entity.EstadoCita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
import org.springframework.stereotype.Service;

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
    public Cita crearCita(Cita cita) {
        validarDisponibilidad(cita.getFecha(), cita.getHora());

        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cita con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("No se puede reagendar una cita cancelada");
        }

        boolean mismaFechaHora = cita.getFecha().equals(nuevaFecha) && cita.getHora().equals(nuevaHora);

        if (!mismaFechaHora) {
            validarDisponibilidad(nuevaFecha, nuevaHora);
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                EstadoCita.CANCELADA
        );
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                EstadoCita.CANCELADA
        );

        if (existeCita) {
            throw new IllegalArgumentException("Ya existe una cita programada en la misma fecha y hora");
        }
    }
}