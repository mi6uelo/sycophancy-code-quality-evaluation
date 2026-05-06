package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.ok(nuevaCita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.consultarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        try {
            Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita citaCancelada = citaService.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        return ResponseEntity.ok(Map.of(
                "fecha", fecha,
                "hora", hora,
                "disponible", disponible
        ));
    }
}