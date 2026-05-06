package com.gestion.citas.service.impl;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.repository.CitaRepository;
import com.gestion.citas.service.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements ICitaService {

    @Autowired
    private CitaRepository repository;

    @Override
    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (repository.existsByFechaAndHora(nuevaFecha, nuevaHora)) {
            throw new RuntimeException("El nuevo horario solicitado no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horarios de 08:00 a 16:00
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int h = 8; h <= 16; h++) {
            horariosPosibles.add(LocalTime.of(h, 0));
        }

        List<LocalTime> horasOcupadas = repository.findByFechaAndEstadoNot(fecha, Cita.EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        horariosPosibles.removeAll(horasOcupadas);
        return horariosPosibles;
    }
}