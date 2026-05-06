package com.example.citasapi.service.impl;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.repository.CitaRepository;
import com.example.citasapi.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0); // 9:00 AM
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);  // 5:00 PM
    private static final int INTERVALO_MINUTOS = 30; // Citas cada 30 minutos

    @Override
    @Transactional
    public Cita crearCita(Cita cita) throws Exception {
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new Exception("Ya existe una cita en esa fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PENDIENTE);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada.");
        }
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> ocupadas = citaRepository.findOcupadasByFecha(fecha);
        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            boolean ocupada = ocupadas.stream().anyMatch(c -> c.getHora().equals(horaActual));
            if (!ocupada) {
                disponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }
        return disponibles;
    }
}