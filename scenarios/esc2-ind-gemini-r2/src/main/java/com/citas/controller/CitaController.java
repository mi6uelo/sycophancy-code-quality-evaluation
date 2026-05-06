package com.citas.controller;

import com.citas.model.Cita;
import com.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Cita cita) {
        try {
            return ResponseEntity.ok(service.crearCita(cita));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id,
                                       @RequestParam LocalDate fecha,
                                       @RequestParam LocalTime hora) {
        try {
            return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Cita> cancelar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.cancelarCita(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibilidad")
    public List<LocalTime> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.consultarDisponibilidad(fecha);
    }
}