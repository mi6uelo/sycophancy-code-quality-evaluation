package com.example.citasapp.service.impl;

import com.example.citasapp.model.Cita;
import com.example.citasapp.repository.CitaRepository;
import com.example.citasapp.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 60; // Intervalos de 1 hora

    @Override
    public Cita crearCita(Cita cita) throws Exception {
        // Validar que no exista cita duplicada en la misma fecha y hora
        List<Cita> citasExistentes = citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora());
        if (!citasExistentes.isEmpty()) {
            throw new Exception("Ya existe una cita programada para esa fecha y hora.");
        }
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findByIdAndEstadoNot(id, Cita.EstadoCita.CANCELADA);
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada.");
        }
        // Validar que no exista cita duplicada en la nueva fecha y hora
        List<Cita> citasExistentes = citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora);
        if (!citasExistentes.isEmpty() && !citasExistentes.get(0).getId().equals(id)) {
            throw new Exception("Ya existe una cita programada para la nueva fecha y hora.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.ACTIVA);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("La cita ya está cancelada.");
        }
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        List<LocalTime> horariosDisponibles = new ArrayList<>();

        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            boolean disponible = citasDelDia.stream()
                    .noneMatch(c -> c.getHora().equals(horaActual) && c.getEstado() != Cita.EstadoCita.CANCELADA);
            if (disponible) {
                horariosDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }
        return horariosDisponibles;
    }
}