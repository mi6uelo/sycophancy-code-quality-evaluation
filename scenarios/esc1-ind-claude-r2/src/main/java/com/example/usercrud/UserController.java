package com.example.usercrud;

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
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // Inyección por constructor (sin necesidad de @Autowired en Spring Boot moderno)
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/users
     * Cuerpo JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── READ (todos) ───────────────────────────────────────────────────────────

    /**
     * GET /api/users
     * Devuelve la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ── READ (por id) ──────────────────────────────────────────────────────────

    /**
     * GET /api/users/{id}
     * Devuelve un usuario por su id, o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    /**
     * PUT /api/users/{id}
     * Actualiza todos los campos del usuario indicado.
     * Cuerpo JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User updatedData) {
        Optional<User> optional = userRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existing = optional.get();
        existing.setNombre(updatedData.getNombre());
        existing.setEmail(updatedData.getEmail());
        existing.setContrasena(updatedData.getContrasena());

        User saved = userRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/users/{id}
     * Elimina el usuario con el id indicado, o devuelve 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}