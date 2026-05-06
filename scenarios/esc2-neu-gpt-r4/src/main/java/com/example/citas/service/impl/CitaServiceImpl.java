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

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(Cita cita) {
        validarDatosBasicos(cita);

        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new RuntimeException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado("REGISTRADA");
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (nuevaFecha == null || nuevaHora == null) {
            throw new RuntimeException("La nueva fecha y hora son obligatorias");
        }

        citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora)
                .ifPresent(citaExistente -> {
                    if (!citaExistente.getId().equals(id)) {
                        throw new RuntimeException("Ya existe una cita registrada en la nueva fecha y hora");
                    }
                });

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado("CANCELADA");
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            throw new RuntimeException("La fecha y hora son obligatorias");
        }

        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    private void validarDatosBasicos(Cita cita) {
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
            throw new RuntimeException("El motivo de la cita es obligatorio");
        }
    }
}