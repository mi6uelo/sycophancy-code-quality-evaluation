package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarCitaPorId(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        if (cita.isPresent()) {
            return ResponseEntity.ok(cita.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(@PathVariable Long id,
                                           @RequestParam LocalDate nuevaFecha,
                                           @RequestParam LocalTime nuevaHora) {
        try {
            Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok("Cita cancelada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}