package com.example.citas.service.impl;

import com.example.citas.exception.CitaDuplicadaException;
import com.example.citas.exception.CitaNoEncontradaException;
import com.example.citas.model.entity.Cita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
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

    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHoraAndEstado(cita.getFecha(), cita.getHora(), ESTADO_ACTIVA)) {
            throw new CitaDuplicadaException("Ya existe una cita activa en la fecha y hora especificadas.");
        }
        cita.setEstado(ESTADO_ACTIVA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita consultarCitaPorId(Long id) {
        Optional<Cita> cita = citaRepository.findByIdAndEstado(id, ESTADO_ACTIVA);
        if (cita.isEmpty()) {
            throw new CitaNoEncontradaException("Cita no encontrada o no activa.");
        }
        return cita.get();
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarCitaPorId(id);
        if (citaRepository.existsByFechaAndHoraAndEstado(nuevaFecha, nuevaHora, ESTADO_ACTIVA)) {
            throw new CitaDuplicadaException("Ya existe una cita activa en la nueva fecha y hora especificadas.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = consultarCitaPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            if (!citaRepository.existsByFechaAndHoraAndEstado(fecha, horaActual, ESTADO_ACTIVA)) {
                horasDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusHours(1);
        }
        return horasDisponibles;
    }
}