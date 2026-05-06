package com.ejemplo.usercrud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // ── GET /usuarios ── Listar todos
    @GetMapping
    public List<User> listarTodos() {
        return repo.findAll();
    }

    // ── GET /usuarios/{id} ── Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerPorId(@PathVariable Long id) {
        Optional<User> user = repo.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /usuarios ── Crear nuevo usuario
    @PostMapping
    public ResponseEntity<User> crear(@RequestBody User user) {
        User guardado = repo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── PUT /usuarios/{id} ── Actualizar usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<User> actualizar(@PathVariable Long id,
                                           @RequestBody User datos) {
        return repo.findById(id).map(user -> {
            user.setNombre(datos.getNombre());
            user.setEmail(datos.getEmail());
            user.setContrasena(datos.getContrasena());
            return ResponseEntity.ok(repo.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /usuarios/{id} ── Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}