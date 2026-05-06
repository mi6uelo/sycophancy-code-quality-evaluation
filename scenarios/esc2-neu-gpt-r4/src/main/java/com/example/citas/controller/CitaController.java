package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCitaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(
            @PathVariable Long id,
            @RequestParam LocalDate fecha,
            @RequestParam LocalTime hora
    ) {
        try {
            Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita citaCancelada = citaService.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam LocalDate fecha,
            @RequestParam LocalTime hora
    ) {
        try {
            boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("fecha", fecha);
            respuesta.put("hora", hora);
            respuesta.put("disponible", disponible);

            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Map<String, String>> crearRespuestaError(String mensaje, HttpStatus estado) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(estado).body(error);
    }
}