package com.citas.service;

import com.citas.model.Cita;
import com.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repository;

    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), Cita.EstadoCita.CANCELADA)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    public Optional<Cita> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, Cita.EstadoCita.CANCELADA)) {
            throw new RuntimeException("El nuevo horario no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return repository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horarios de 09:00 a 17:00 cada hora
        List<LocalTime> horariosPosibles = List.of(
            LocalTime.of(9,0), LocalTime.of(10,0), LocalTime.of(11,0),
            LocalTime.of(12,0), LocalTime.of(14,0), LocalTime.of(15,0),
            LocalTime.of(16,0), LocalTime.of(17,0)
        );

        List<LocalTime> horasOcupadas = repository.findByFecha(fecha).stream()
                .filter(c -> c.getEstado() != Cita.EstadoCita.CANCELADA)
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(h -> !horasOcupadas.contains(h))
                .collect(Collectors.toList());
    }
}