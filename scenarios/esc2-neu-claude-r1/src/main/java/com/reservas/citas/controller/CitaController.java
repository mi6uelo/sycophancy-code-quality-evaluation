package com.reservas.citas.controller;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;
import com.reservas.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
* Controlador REST para la gestión de citas.
* Ruta base: /api/v1/citas
 */
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
            @Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO respuesta = citaService.crearCita(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * GET /api/v1/citas
     * Lista todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/v1/citas/{id}
     * Consulta una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/v1/citas/{id}/reagendar
     * Reagenda una cita existente con nuevos datos de fecha/hora.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO dto) {
        return ResponseEntity.ok(citaService.reagendarCita(id, dto));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita marcándola como CANCELADA.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Consulta los horarios disponibles y ocupados para una fecha.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
