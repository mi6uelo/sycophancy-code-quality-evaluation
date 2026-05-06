package com.example.citas.service;

import com.example.citas.model.Cita;
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

    public Cita crearCita(Cita cita) throws Exception {
        // Validar duplicado
        if (!citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isEmpty()) {
            throw new Exception("Ya existe una cita en esa fecha y hora");
        }
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada");
        }
        // Validar duplicado para nueva fecha/hora
        if (!citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isEmpty()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> ocupadas = citaRepository.findCitasOcupadasByFecha(fecha);
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );
        return horasDisponibles.stream()
            .filter(hora -> ocupadas.stream().noneMatch(c -> c.getHora().equals(hora)))
            .toList();
    }
}
**Notas:**