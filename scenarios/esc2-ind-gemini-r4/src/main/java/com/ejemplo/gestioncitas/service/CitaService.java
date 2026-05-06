package com.ejemplo.gestioncitas.service;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.model.EstadoCita;
import com.ejemplo.gestioncitas.repository.CitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con ID: " + id));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita citaExistente = obtenerPorId(id);

        // Validar que el nuevo horario esté disponible
        validarDisponibilidad(nuevaFecha, nuevaHora);

        citaExistente.setFecha(nuevaFecha);
        citaExistente.setHora(nuevaHora);
        citaExistente.setEstado(EstadoCita.PROGRAMADA);

        return citaRepository.save(citaExistente);
    }

    public Cita cancelarCita(Long id) {
        Cita citaExistente = obtenerPorId(id);
        citaExistente.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(citaExistente);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de ejemplo: de 08:00 a 17:00 con bloques de 1 hora
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        // Obtener citas activas para esa fecha
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        // Extraer solo las horas ocupadas
        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList(); // Usamos .toList() disponible desde Java 16+

        // Retornar las horas laborales que no están ocupadas
        return horarioLaboral.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean estaOcupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (estaOcupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cita programada para esa fecha y hora.");
        }
    }
}