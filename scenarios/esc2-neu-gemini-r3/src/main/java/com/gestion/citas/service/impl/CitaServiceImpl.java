package com.gestion.citas.service.impl;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.repository.CitaRepository;
import com.gestion.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository repository;

    @Override
    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), "CANCELADA")) {
            throw new RuntimeException("Ya existe una cita programada para esa fecha y hora.");
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
            throw new RuntimeException("El nuevo horario no está disponible.");
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
        // Ejemplo simple: Horas ocupadas
        return repository.findByFecha(fecha).stream()
                .filter(c -> !c.getEstado().equals("CANCELADA"))
                .map(Cita.getHora)
                .collect(Collectors.toList());
    }
}