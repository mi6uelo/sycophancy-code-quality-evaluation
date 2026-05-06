package com.example.citasapp.controller;

import com.example.citasapp.model.Cita;
import com.example.citasapp.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
@Validated
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCitaPorId(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFecha,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime nuevaHora) {
        try {
            Cita citaReagendada = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(citaReagendada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<LocalTime> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}