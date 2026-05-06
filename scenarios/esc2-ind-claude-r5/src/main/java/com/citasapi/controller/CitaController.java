package com.citasapi.controller;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;
import com.citasapi.service.CitaService;
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
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    /**
     * POST /api/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/citas
     * Lista todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/citas/{id}
     * Consulta una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/citas/{id}/reagendar
     * Reagenda una cita existente a una nueva fecha y hora.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/citas/{id}/cancelar
     * Cancela una cita existente.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/citas/disponibilidad?fecha=2026-05-10
     * Consulta los horarios disponibles para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}