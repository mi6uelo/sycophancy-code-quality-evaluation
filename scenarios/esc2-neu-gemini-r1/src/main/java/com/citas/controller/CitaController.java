package com.citas.controller;

import com.citas.model.entity.Cita;
import com.citas.service.ICitaService;
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
    private ICitaService service;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Cita cita) {
        try {
            return new ResponseEntity<>(service.crearCita(cita), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            LocalDate fecha = LocalDate.parse(body.get("fecha").toString());
            LocalTime hora = LocalTime.parse(body.get("hora").toString());
            return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public List<LocalTime> disponibilidad(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.consultarDisponibilidad(fecha);
    }
}