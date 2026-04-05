package com.uisrael.inducido.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.uisrael.inducido.model.entity.User;
import com.uisrael.inducido.repository.UserRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ──────────────────────────────────────────────
    // GET /api/usuarios  → listar todos
    // ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ──────────────────────────────────────────────
    // GET /api/usuarios/{id}  → obtener por id
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return buildError("Usuario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user.get());
    }

    // ──────────────────────────────────────────────
    // POST /api/usuarios  → crear usuario
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        // Validaciones
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return buildError("El email no puede estar vacío.", HttpStatus.BAD_REQUEST);
        }
        if (user.getContrasena() == null || user.getContrasena().length() < 8) {
            return buildError("La contraseña debe tener al menos 8 caracteres.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return buildError("Ya existe un usuario con ese email.", HttpStatus.CONFLICT);
        }

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ──────────────────────────────────────────────
    // PUT /api/usuarios/{id}  → actualizar usuario
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User updatedData) {
        // Validaciones
        if (updatedData.getEmail() == null || updatedData.getEmail().isBlank()) {
            return buildError("El email no puede estar vacío.", HttpStatus.BAD_REQUEST);
        }
        if (updatedData.getContrasena() == null || updatedData.getContrasena().length() < 8) {
            return buildError("La contraseña debe tener al menos 8 caracteres.", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return buildError("Usuario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }

        // Verificar email duplicado (ignorando el propio usuario)
        Optional<User> byEmail = userRepository.findByEmail(updatedData.getEmail());
        if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
            return buildError("Ya existe otro usuario con ese email.", HttpStatus.CONFLICT);
        }

        User user = existing.get();
        user.setNombre(updatedData.getNombre());
        user.setEmail(updatedData.getEmail());
        user.setContrasena(updatedData.getContrasena());

        return ResponseEntity.ok(userRepository.save(user));
    }

    // ──────────────────────────────────────────────
    // DELETE /api/usuarios/{id}  → eliminar usuario
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return buildError("Usuario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado correctamente.");
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────
    // Helper: construir respuesta de error
    // ──────────────────────────────────────────────
    private ResponseEntity<Map<String, String>> buildError(String mensaje, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(status).body(error);
    }
}