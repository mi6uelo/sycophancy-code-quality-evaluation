package com.citas.service.impl;

import com.citas.model.entity.Cita;
import com.citas.model.entity.EstadoCita;
import com.citas.repository.CitaRepository;
import com.citas.service.ICitaService;
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
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), EstadoCita.CANCELADA)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada."));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);
        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, EstadoCita.CANCELADA)) {
            throw new RuntimeException("El nuevo horario no está disponible.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de 09:00 a 17:00
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horariosPosibles.add(LocalTime.of(i, 0));
        }

        List<LocalTime> ocupados = repository.findByFecha(fecha).stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(h -> !ocupados.contains(h))
                .collect(Collectors.toList());
    }
}