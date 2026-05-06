package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.ok(nuevaCita);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCitaPorId(@PathVariable Long id) {
        Cita cita = citaService.consultarCitaPorId(id);
        return ResponseEntity.ok(cita);
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestParam LocalDate nuevaFecha, @RequestParam LocalTime nuevaHora) {
        Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
        return ResponseEntity.ok(cita);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}