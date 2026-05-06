package com.empresa.citasapi.controller;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestBody ReagendarDTO datos) {
        Cita citaActualizada = citaService.reagendarCita(id, datos.fecha(), datos.hora());
        return ResponseEntity.ok(citaActualizada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horariosLibres = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horariosLibres);
    }
}