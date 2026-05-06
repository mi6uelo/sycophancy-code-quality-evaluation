package com.example.citasbackend.service.impl;

import com.example.citasbackend.model.entity.Cita;
import com.example.citasbackend.repository.CitaRepository;
import com.example.citasbackend.service.CitaService;
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

    @Override
    public Cita crearCita(Cita cita) throws Exception {
        // Validar duplicado
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new Exception("Ya existe una cita en esa fecha y hora");
        }
        cita.setEstado(Cita.EstadoCita.RESERVADA);
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
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada");
        }
        // Verificar si la nueva fecha/hora está disponible
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        List<LocalTime> horariosOcupados = citasDelDia.stream()
                .filter(c -> c.getEstado() == Cita.EstadoCita.RESERVADA)
                .map(Cita::getHora)
                .toList();

        // Asumiendo horarios de 9:00 a 17:00 cada hora
        List<LocalTime> horariosDisponibles = new ArrayList<>();
        LocalTime horaActual = LocalTime.of(9, 0);
        LocalTime horaFin = LocalTime.of(17, 0);
        while (horaActual.isBefore(horaFin)) {
            if (!horariosOcupados.contains(horaActual)) {
                horariosDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusHours(1);
        }
        return horariosDisponibles;
    }
}