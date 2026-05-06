package com.sistema.citas.service;

import com.sistema.citas.model.Cita;
import com.sistema.citas.model.EstadoCita;
import com.sistema.citas.repository.CitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public Cita crearCita(Cita cita) {
        validarDisponibilidad(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Cita consultarPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reagendar una cita cancelada");
        }

        // Si cambia la fecha o la hora, validamos disponibilidad
        if (!cita.getFecha().equals(nuevaFecha) || !cita.getHora().equals(nuevaHora)) {
            validarDisponibilidad(nuevaFecha, nuevaHora);
            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
            cita.setEstado(EstadoCita.REAGENDADA);
        }

        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = consultarPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de ejemplo: 08:00 a 17:00
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        // Obtener citas activas para ese día
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        // Filtrar las horas que ya están ocupadas
        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList(); // Característica optimizada en Java recientes

        horarioLaboral.removeAll(horasOcupadas);
        return horarioLaboral;
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cita programada para esa fecha y hora.");
        }
    }
}