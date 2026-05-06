package com.citasapi.controller;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;
import com.citasapi.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * POST /api/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO requestDTO) {

        CitaResponseDTO response = citaService.crearCita(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/citas
     * Devuelve todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/citas/{id}
     * Devuelve una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/citas/{id}/reagendar
     * Modifica la fecha y hora de una cita existente.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarDTO reagendarDTO) {

        return ResponseEntity.ok(citaService.reagendarCita(id, reagendarDTO));
    }

    /**
     * PATCH /api/citas/{id}/cancelar
     * Cancela una cita sin eliminarla del sistema.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/citas/disponibilidad?fecha=YYYY-MM-DD
     * Devuelve los slots ya ocupados en una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<CitaResponseDTO>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}