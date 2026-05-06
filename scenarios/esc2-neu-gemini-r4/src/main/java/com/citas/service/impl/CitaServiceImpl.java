package com.citas.service.impl;

import com.citas.model.entity.Cita;
import com.citas.repository.CitaRepository;
import com.citas.service.CitaService;
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
    private CitaRepository citaRepository;

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), "CANCELADA")) {
            throw new RuntimeException("Ya existe una cita programada para esa fecha y hora.");
        }
        cita.setEstado("PROGRAMADA");
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (citaRepository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, "CANCELADA")) {
            throw new RuntimeException("El nuevo horario solicitado no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horario de 09:00 a 17:00 cada hora
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        List<LocalTime> horasOcupadas = citaRepository.findByFecha(fecha).stream()
                .filter(c -> !c.getEstado().equals("CANCELADA"))
                .map(Cita::getHora)
                .toList();

        return horarioLaboral.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }
}