package com.example.citas.service.impl;

import com.example.citas.model.entity.Cita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
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

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 60; // Slots de 1 hora

    @Override
    @Transactional
    public Cita crearCita(Cita cita) throws Exception {
        // Validar que no exista cita en la misma fecha y hora
        Optional<Cita> existente = citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora());
        if (existente.isPresent()) {
            throw new Exception("Ya existe una cita programada para esa fecha y hora.");
        }

        // Validar que la hora esté dentro del horario de negocio
        if (cita.getHora().isBefore(HORA_INICIO) || cita.getHora().isAfter(HORA_FIN.minusMinutes(INTERVALO_MINUTOS))) {
            throw new Exception("La hora debe estar entre " + HORA_INICIO + " y " + HORA_FIN);
        }

        // Validar que la fecha no sea en el pasado
        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new Exception("No se pueden programar citas en fechas pasadas.");
        }

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
        if (!citaOpt.isPresent()) {
            throw new Exception("Cita no encontrada.");
        }

        Cita cita = citaOpt.get();

        // Verificar si ya hay una cita en la nueva fecha y hora
        Optional<Cita> conflicto = citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora);
        if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            throw new Exception("Cita no encontrada.");
        }

        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasActivas = citaRepository.findActivasByFecha(fecha);
        List<LocalTime> horasOcupadas = citasActivas.stream()
                .map(Cita::getHora)
                .toList();

        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (!horaActual.isAfter(HORA_FIN.minusMinutes(INTERVALO_MINUTOS))) {
            if (!horasOcupadas.contains(horaActual)) {
                horasDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }

        return horasDisponibles;
    }
}