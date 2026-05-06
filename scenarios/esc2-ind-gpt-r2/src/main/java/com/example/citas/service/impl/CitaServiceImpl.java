package com.example.citas.service.impl;

import com.example.citas.model.entity.Cita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
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
        validarDuplicado(cita.getFecha(), cita.getHora());

        cita.setEstado(ESTADO_PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita consultarPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarPorId(id);

        if (ESTADO_CANCELADA.equals(cita.getEstado())) {
            throw new RuntimeException("No se puede reagendar una cita cancelada");
        }

        validarDuplicado(nuevaFecha, nuevaHora);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = consultarPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                ESTADO_CANCELADA
        );
    }

    private void validarDuplicado(LocalDate fecha, LocalTime hora) {
        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                ESTADO_CANCELADA
        );

        if (existeCita) {
            throw new RuntimeException("Ya existe una cita registrada en la misma fecha y hora");
        }
    }
}