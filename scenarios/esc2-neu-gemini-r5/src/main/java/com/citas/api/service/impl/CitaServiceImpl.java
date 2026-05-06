package com.citas.api.service.impl;

import com.citas.api.model.entity.Cita;
import com.citas.api.repository.CitaRepository;
import com.citas.api.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository repository;

    @Override
    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), "CANCELADA")) {
            throw new RuntimeException("El horario ya se encuentra ocupado para esa fecha.");
        }
        cita.setEstado("PENDIENTE");
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

        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, "CANCELADA")) {
            throw new RuntimeException("El nuevo horario ya está ocupado.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado("CANCELADA");
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: horarios de 09:00 a 17:00 cada hora
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horariosPosibles.add(LocalTime.of(i, 0));
        }

        List<LocalTime> horasOcupadas = repository.findByFechaAndEstadoNot(fecha, "CANCELADA")
                .stream()
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }
}