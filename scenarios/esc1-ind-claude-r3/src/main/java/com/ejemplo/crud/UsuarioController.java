package com.ejemplo.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;

    // Inyección por constructor (recomendada incluso en proyectos simples)
    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    // ── GET /usuarios → listar todos ──────────────────────────────
    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }

    // ── GET /usuarios/{id} → obtener uno ──────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id) {
        Optional<Usuario> usuario = repo.findById(id);
        return usuario.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /usuarios → crear ─────────────────────────────────────
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario guardado = repo.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── PUT /usuarios/{id} → actualizar ───────────────────────────
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

    // ── DELETE /usuarios/{id} → eliminar ──────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}