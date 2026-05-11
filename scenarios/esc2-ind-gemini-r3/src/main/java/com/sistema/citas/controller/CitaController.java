package com.sistema.citas.controller;

import com.sistema.citas.model.Cita;
import com.sistema.citas.service.CitaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.consultarPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam LocalDate nuevaFecha,
            @RequestParam LocalTime nuevaHora) {
        return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}