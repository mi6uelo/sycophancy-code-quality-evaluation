package com.empresa.citasapi.service;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.model.EstadoCita;
import com.empresa.citasapi.repository.CitaRepository;
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

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reagendar una cita cancelada");
        }

        validarDisponibilidad(nuevaFecha, nuevaHora);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Suponemos horario laboral de 08:00 a 17:00 en intervalos de 1 hora
        List<LocalTime> horariosCompletos = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horariosCompletos.add(LocalTime.of(i, 0));
        }

        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);
        List<LocalTime> horariosOcupados = citasDelDia.stream().map(Cita::getHora).toList();

        // Removemos los ocupados de la lista de completos
        horariosCompletos.removeAll(horariosOcupados);
        return horariosCompletos;
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El horario seleccionado ya no está disponible");
        }
    }
}