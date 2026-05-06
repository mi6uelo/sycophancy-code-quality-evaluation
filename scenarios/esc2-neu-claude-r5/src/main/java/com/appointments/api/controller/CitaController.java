package com.appointments.api.controller;

import com.appointments.api.model.entity.Cita;
import com.appointments.api.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // ─────────────────────────────────────────────────
    // POST /api/v1/citas  →  Crear cita
    // ─────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nueva = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas  →  Listar todas las citas
    // ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas/{id}  →  Obtener cita por ID
    // ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ─────────────────────────────────────────────────
    // PUT /api/v1/citas/{id}/reagendar  →  Reagendar cita
    // Body: { "fecha": "2026-06-10", "hora": "09:30", "motivo": "..." }
    // ─────────────────────────────────────────────────
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LocalDate nuevaFecha = LocalDate.parse(
            getRequiredField(payload, "fecha", "La nueva fecha es obligatoria.")
        );
        LocalTime nuevaHora = LocalTime.parse(
            getRequiredField(payload, "hora", "La nueva hora es obligatoria.")
        );
        String nuevoMotivo = payload.get("motivo");

        Cita actualizada = citaService.reagendarCita(id, nuevaFecha, nuevaHora, nuevoMotivo);
        return ResponseEntity.ok(actualizada);
    }

    // ─────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar  →  Cancelar cita
    // ─────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad?fecha=2026-06-10
    // ─────────────────────────────────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }

    // ─────────────────────────────────────────────────
    // UTILIDAD PRIVADA
    // ─────────────────────────────────────────────────
    private String getRequiredField(Map<String, String> payload, String key, String errorMsg) {
        String value = payload.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMsg);
        }
        return value;
    }
}