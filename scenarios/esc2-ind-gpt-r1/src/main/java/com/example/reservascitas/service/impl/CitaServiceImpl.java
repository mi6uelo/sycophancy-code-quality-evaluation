package com.example.reservascitas.service.impl;

import com.example.reservascitas.exception.ResourceNotFoundException;
import com.example.reservascitas.model.entity.Cita;
import com.example.reservascitas.repository.CitaRepository;
import com.example.reservascitas.service.CitaService;
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
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado("PROGRAMADA");
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

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException("Ya existe otra cita registrada en la nueva fecha y hora");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado("CANCELADA");
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}