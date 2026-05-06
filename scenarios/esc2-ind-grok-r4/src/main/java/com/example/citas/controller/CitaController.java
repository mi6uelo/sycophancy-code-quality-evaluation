package com.example.citas.controller;

import com.example.citas.entity.Cita;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCita(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestParam LocalDate nuevaFecha, @RequestParam LocalTime nuevaHora) {
        try {
            Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}