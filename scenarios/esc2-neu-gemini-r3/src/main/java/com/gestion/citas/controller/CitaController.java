package com.gestion.citas.controller;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Cita cita) {
        try {
            return ResponseEntity.ok(service.crearCita(cita));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id,
                                       @RequestParam LocalDate fecha,
                                       @RequestParam LocalTime hora) {
        try {
            return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> verDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.consultarDisponibilidad(fecha));
    }
}