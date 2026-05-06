package com.citas.api.controller;

import com.citas.api.model.entity.Cita;
import com.citas.api.service.CitaService;
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
    public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
        return ResponseEntity.ok(service.crearCita(cita));
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
    public ResponseEntity<Cita> reagendar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
        LocalTime hora = LocalTime.parse((String) payload.get("hora"));

        return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.consultarDisponibilidad(fecha));
    }
}