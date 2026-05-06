package com.gestion.citas.controller;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
* Controlador REST para la gestión de citas.
* Base URL: /api/v1/citas
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/citas  →  Crear cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Crea una nueva cita.
     *
     * Ejemplo Body JSON:
     * {
     *   "nombreCliente": "Juan Pérez",
     *   "fecha": "2026-05-20",
     *   "hora": "10:30:00",
     *   "motivo": "Consulta general"
     * }
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        log.info("POST /api/v1/citas - Crear cita para '{}'", cita.getNombreCliente());
        Cita citaCreada = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas  →  Listar citas
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lista todas las citas registradas en el sistema.
     */
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        log.info("GET /api/v1/citas - Listar todas las citas");
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/{id}  →  Obtener cita por ID
    // ─────────────────────────────────────────────────────────────────

    /**
     * Retorna una cita específica por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        log.info("GET /api/v1/citas/{} - Obtener cita por ID", id);
        Cita cita = citaService.obtenerCitaPorId(id);
        return ResponseEntity.ok(cita);
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/reagendar  →  Reagendar cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Reagenda una cita existente a un nuevo slot de fecha y hora.
     *
     * Parámetros de query:
     *   nuevaFecha  (yyyy-MM-dd)  — requerido
     *   nuevaHora   (HH:mm:ss)   — requerido
     *
     * Ejemplo:
     *   PATCH /api/v1/citas/3/reagendar?nuevaFecha=2026-06-01&nuevaHora=14:00:00
     */
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                LocalDate nuevaFecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                LocalTime nuevaHora) {

        log.info("PATCH /api/v1/citas/{}/reagendar → {} {}", id, nuevaFecha, nuevaHora);
        Cita citaReagendada = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
        return ResponseEntity.ok(citaReagendada);
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar  →  Cancelar cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cancela una cita estableciendo su estado como CANCELADA.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        log.info("PATCH /api/v1/citas/{}/cancelar", id);
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad  →  Consultar disponibilidad
    // ─────────────────────────────────────────────────────────────────

    /**
     * Consulta los horarios ocupados y disponibles para una fecha específica.
     *
     * Parámetro de query:
     *   fecha  (yyyy-MM-dd) — requerido
     *
     * Ejemplo:
     *   GET /api/v1/citas/disponibilidad?fecha=2026-05-20
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                LocalDate fecha) {

        log.info("GET /api/v1/citas/disponibilidad?fecha={}", fecha);
        Map<String, Object> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}