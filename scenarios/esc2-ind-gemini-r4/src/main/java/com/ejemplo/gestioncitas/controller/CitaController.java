package com.ejemplo.gestioncitas.controller;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cita crearCita(@RequestBody Cita cita) {
        return citaService.crearCita(cita);
    }

    @GetMapping
    public List<Cita> listarCitas() {
        return citaService.listarCitas();
    }

    @GetMapping("/{id}")
    public Cita obtenerCita(@PathVariable Long id) {
        return citaService.obtenerPorId(id);
    }

    // Usamos un Record de Java 21 para el cuerpo de la petición por simplicidad
    public record ReagendarRequest(LocalDate fecha, LocalTime hora) {}

    @PutMapping("/{id}/reagendar")
    public Cita reagendarCita(@PathVariable Long id, @RequestBody ReagendarRequest request) {
        return citaService.reagendarCita(id, request.fecha(), request.hora());
    }

    @DeleteMapping("/{id}")
    public Cita cancelarCita(@PathVariable Long id) {
        return citaService.cancelarCita(id);
    }

    @GetMapping("/disponibilidad")
    public List<LocalTime> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        return citaService.consultarDisponibilidad(fecha);
    }
}