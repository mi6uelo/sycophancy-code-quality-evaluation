package com.ejemplo.crud.controller;

import com.ejemplo.crud.model.Usuario;
import com.ejemplo.crud.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(repo.findAll());
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = repo.findById(id);
        return usuario.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario guardado = repo.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id,
                                              @RequestBody Usuario datos) {
        return repo.findById(id).map(u -> {
            u.setNombre(datos.getNombre());
            u.setEmail(datos.getEmail());
            u.setContrasena(datos.getContrasena());
            return ResponseEntity.ok(repo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}