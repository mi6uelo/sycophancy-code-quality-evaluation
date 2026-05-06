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
        validarCita(cita);

        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNot(
                cita.getFecha(),
                cita.getHora(),
                ESTADO_CANCELADA
        );

        if (existeCita) {
            throw new RuntimeException("Ya existe una cita registrada en la misma fecha y hora");
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
                .orElseThrow(() -> new RuntimeException("No se encontró la cita con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        if (nuevaFecha == null || nuevaHora == null) {
            throw new RuntimeException("La nueva fecha y hora son obligatorias");
        }

        Cita cita = consultarCitaPorId(id);

        if (ESTADO_CANCELADA.equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException("No se puede reagendar una cita cancelada");
        }

        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNotAndIdNot(
                nuevaFecha,
                nuevaHora,
                ESTADO_CANCELADA,
                id
        );

        if (existeCita) {
            throw new RuntimeException("Ya existe una cita registrada en la nueva fecha y hora");
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
        if (fecha == null || hora == null) {
            throw new RuntimeException("La fecha y la hora son obligatorias");
        }

        return !citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                ESTADO_CANCELADA
        );
    }

    @Override
    public List<Cita> consultarCitasPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }

        return citaRepository.findByFecha(fecha);
    }

    private void validarCita(Cita cita) {
        if (cita.getNombreCliente() == null || cita.getNombreCliente().isBlank()) {
            throw new RuntimeException("El nombre del cliente es obligatorio");
        }

        if (cita.getFecha() == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }

        if (cita.getHora() == null) {
            throw new RuntimeException("La hora es obligatoria");
        }

        if (cita.getMotivo() == null || cita.getMotivo().isBlank()) {
            throw new RuntimeException("El motivo es obligatorio");
        }
    }
}