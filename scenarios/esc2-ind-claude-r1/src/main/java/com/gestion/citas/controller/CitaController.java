package com.gestion.citas.controller;

import com.gestion.citas.dto.CitaRequestDTO;
import com.gestion.citas.dto.CitaResponseDTO;
import com.gestion.citas.dto.ReagendarRequestDTO;
import com.gestion.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
* Controlador REST que expone los endpoints de la API de gestión de citas.
* Base URL: /api/v1/citas
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
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO creada = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
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
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PATCH /api/v1/citas/{id}/reagendar
     * Modifica la fecha y/u hora de una cita existente.
     */
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita; no la elimina de la base de datos.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Retorna los horarios libres para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}