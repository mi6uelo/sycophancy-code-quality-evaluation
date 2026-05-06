package com.reservas.citas.controller;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
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

    /**
     * POST /api/v1/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/citas
     * Lista todas las citas registradas ordenadas por fecha y hora.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/v1/citas/{id}
     * Retorna el detalle de una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/v1/citas/{id}/reagendar
     * Reagenda una cita existente (actualiza fecha, hora, cliente y/o motivo).
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita sin eliminarla del sistema.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Retorna los horarios libres disponibles para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}