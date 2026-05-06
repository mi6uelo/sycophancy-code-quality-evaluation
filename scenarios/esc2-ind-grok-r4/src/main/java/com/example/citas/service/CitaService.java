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
        Optional<Cita> existente = citaRepository.findByFechaAndHoraAndEstado(cita.getFecha(), cita.getHora(), "activa");
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una cita activa en esa fecha y hora");
        }
        cita.setEstado("activa");
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (!"activa".equals(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden reagendar citas activas");
        }
        // Validar que el nuevo horario esté disponible
        Optional<Cita> conflicto = citaRepository.findByFechaAndHoraAndEstado(nuevaFecha, nuevaHora, "activa");
        if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
            throw new RuntimeException("El nuevo horario no está disponible");
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
        if (!"activa".equals(cita.getEstado())) {
            throw new RuntimeException("La cita ya no está activa");
        }
        cita.setEstado("cancelada");
        citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Asumimos horario de 9:00 a 18:00, citas de 1 hora
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
            LocalTime.of(17, 0)
        );

        List<Cita> citasActivas = citaRepository.findActiveByFecha(fecha);
        List<LocalTime> horasOcupadas = citasActivas.stream()
            .map(Cita::getHora)
            .toList();

        return horasDisponibles.stream()
            .filter(hora -> !horasOcupadas.contains(hora))
            .toList();
    }
}