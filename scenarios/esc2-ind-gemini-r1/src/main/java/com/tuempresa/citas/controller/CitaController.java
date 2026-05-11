package com.tuempresa.citas.controller;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> crearCita(@RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(@PathVariable Long id, @RequestBody ReagendarDto request) {
        try {
            Cita citaActualizada = citaService.reagendarCita(id, request.fecha(), request.hora());
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita citaCancelada = citaService.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidadHoraria(localDate);
        return ResponseEntity.ok(horasDisponibles);
    }

    // DTO utilizando "record" (Característica de Java 14+, nativo en Java 21)
    public record ReagendarDto(LocalDate fecha, LocalTime hora) {}
}
