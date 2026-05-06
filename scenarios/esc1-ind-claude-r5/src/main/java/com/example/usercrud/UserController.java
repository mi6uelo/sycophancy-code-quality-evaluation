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

    // Inyección por constructor (recomendada sobre @Autowired en campo)
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/users
     * Crea un nuevo usuario.
     * Body JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── READ ALL ───────────────────────────────────────────────────────────────

    /**
     * GET /api/users
     * Devuelve la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ── READ ONE ───────────────────────────────────────────────────────────────

    /**
     * GET /api/users/{id}
     * Devuelve un usuario por su ID.
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
     * Actualiza todos los campos de un usuario existente.
     * Body JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User incoming) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existing.get();
        user.setNombre(incoming.getNombre());
        user.setEmail(incoming.getEmail());
        user.setContrasena(incoming.getContrasena());

        return ResponseEntity.ok(userRepository.save(user));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/users/{id}
     * Elimina un usuario por su ID.
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