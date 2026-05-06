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
            return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(cita));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCitaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuestaError(e.getMessage()));
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos
    ) {
        try {
            LocalDate nuevaFecha = LocalDate.parse(datos.get("fecha"));
            LocalTime nuevaHora = LocalTime.parse(datos.get("hora"));

            return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuestaError(e.getMessage()));
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
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @GetMapping("/fecha")
    public ResponseEntity<?> listarCitasPorFecha(@RequestParam LocalDate fecha) {
        try {
            return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    private Map<String, String> respuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}