package com.example.citasapi.service.impl;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.repository.CitaRepository;
import com.example.citasapi.service.CitaService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado(ESTADO_PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita consultarCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una cita con el ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarCitaPorId(id);

        if (ESTADO_CANCELADA.equalsIgnoreCase(cita.getEstado())) {
            throw new IllegalArgumentException("No se puede reagendar una cita cancelada");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException("Ya existe otra cita registrada en la misma fecha y hora");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(ESTADO_PROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = consultarCitaPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}