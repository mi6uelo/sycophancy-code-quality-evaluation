package com.agenda.citas.controller;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;
import com.agenda.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/citas
    // Crear una nueva cita
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas
    // Listar todas las citas
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/{id}
    // Consultar una cita por ID
    // ────���─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> consultarCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.consultarCitaPorId(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/reagendar
    // Reagendar una cita existente
    // Body: { "fecha": "2026-05-10", "hora": "09:00:00" }
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        LocalDate nuevaFecha = LocalDate.parse(body.get("fecha"));
        LocalTime nuevaHora  = LocalTime.parse(body.get("hora"));

        return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar
    // Cancelar una cita
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad?fecha=2026-05-10
    // Consultar horarios disponibles para una fecha
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}