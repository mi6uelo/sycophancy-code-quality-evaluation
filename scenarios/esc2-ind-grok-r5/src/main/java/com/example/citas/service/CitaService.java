package com.example.citas.service;

import com.example.citas.entity.Cita;
import com.example.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    public Cita crearCita(Cita cita) {
        // Validar que no exista cita en la misma fecha y hora
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new RuntimeException("Ya existe una cita en esa fecha y hora");
        }
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCita(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede reagendar una cita cancelada");
        }
        // Validar que la nueva fecha y hora no esté ocupada
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new RuntimeException("La nueva fecha y hora ya están ocupadas");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    public void cancelarCita(Long id) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        // Asumir horario de 9:00 a 17:00, citas cada hora
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );
        // Filtrar horas ocupadas (solo citas confirmadas)
        return horasDisponibles.stream()
            .filter(hora -> citasDelDia.stream()
                .noneMatch(c -> c.getHora().equals(hora) && c.getEstado() == Cita.EstadoCita.CONFIRMADA))
            .toList();
    }
}