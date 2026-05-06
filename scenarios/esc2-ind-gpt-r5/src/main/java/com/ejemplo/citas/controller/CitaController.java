package com.ejemplo.citas.controller;

import com.ejemplo.citas.model.entity.Cita;
import com.ejemplo.citas.service.CitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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
        Cita citaCreada = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
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
            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora
    ) {
        Cita citaReagendada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaReagendada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora
    ) {
        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha);
        respuesta.put("hora", hora);
        respuesta.put("disponible", disponible);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }
}