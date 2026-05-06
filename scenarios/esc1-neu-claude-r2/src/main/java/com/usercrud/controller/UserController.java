package com.usercrud.controller;

import com.usercrud.model.entity.User;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ──────────────────────────────────────────────────────────────
    // GET ALL  →  GET /api/users
    // ──────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ──────────────────────────────────────────────────────────────
    // GET BY ID  →  GET /api/users/{id}
    // ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE  →  POST /api/users
    // ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE  →  PUT /api/users/{id}
    // ────────────────────────────────────────���─────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE  →  DELETE /api/users/{id}
    // ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════
    // MANEJO DE ERRORES
    // ══════════════════════════════════════════════════════════════

    /**
     * Captura errores de validación de Bean Validation (@Valid).
     * Devuelve un mapa campo → mensaje de error con HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Captura intentos de acceder a un usuario inexistente.
     * Devuelve HTTP 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(
            EntityNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Captura errores de negocio, como email duplicado.
     * Devuelve HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
