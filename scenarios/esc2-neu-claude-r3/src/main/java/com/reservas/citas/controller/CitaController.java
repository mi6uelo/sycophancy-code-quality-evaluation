package com.reservas.citas.controller;

import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;
import com.reservas.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ── POST /api/v1/citas ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/v1/citas ─────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ── GET /api/v1/citas/{id} ────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ── PUT /api/v1/citas/{id}/reagendar ──────────────────────────────────────
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarDTO dto) {

        return ResponseEntity.ok(citaService.reagendarCita(id, dto));
    }

    // ── PATCH /api/v1/citas/{id}/cancelar ────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ── GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD ────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}