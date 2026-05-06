package com.citas.controller;

import com.citas.model.entity.Cita;
import com.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
        return new ResponseEntity<>(citaService.crearCita(cita), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Cita> listar() {
        return citaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
        LocalTime hora = LocalTime.parse((String) payload.get("hora"));
        return ResponseEntity.ok(citaService.reagendarCita(id, fecha, hora));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}