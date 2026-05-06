package com.ejemplo.reservas.controller;

import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.service.CitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaActualizada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        return ResponseEntity.ok(Map.of(
                "fecha", fecha,
                "hora", hora,
                "disponible", disponible
        ));
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }
}