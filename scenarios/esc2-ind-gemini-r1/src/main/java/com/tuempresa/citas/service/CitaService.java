package com.tuempresa.citas.service;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.model.EstadoCita;
import com.tuempresa.citas.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    // Horarios de atención: de 09:00 a 17:00
    private static final List<LocalTime> HORARIO_LABORAL = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
    );

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public Cita crearCita(Cita cita) {
        validarDisponibilidadExacta(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> obtenerCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        // Solo se valida si la fecha u hora cambiaron
        if (!cita.getFecha().equals(nuevaFecha) || !cita.getHora().equals(nuevaHora)) {
            validarDisponibilidadExacta(nuevaFecha, nuevaHora);
            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
        }

        cita.setEstado(EstadoCita.PROGRAMADA); // Regresa a programada si estaba en otro estado
        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidadHoraria(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList();

        // Filtra las horas laborales dejando solo las que no están ocupadas
        return HORARIO_LABORAL.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .toList();
    }

    private void validarDisponibilidadExacta(LocalDate fecha, LocalTime hora) {
        boolean estaOcupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (estaOcupado) {
            throw new RuntimeException("Ya existe una cita programada para la fecha " + fecha + " a las " + hora);
        }
    }
}