package com.example.usercrud.controller;

import com.example.usercrud.model.entity.User;
import com.example.usercrud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── GET /api/v1/users ─────────────────────────────────────────────────────
    // Retorna la lista completa de usuarios

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ── GET /api/v1/users/{id} ────────────────────────────────────────────────
    // Retorna un usuario por su ID

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ── POST /api/v1/users ────────────────────────────────────────────────────
    // Crea un nuevo usuario; valida el cuerpo con @Valid

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = null;
		try {
			created = userService.create(user);
		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── PUT /api/v1/users/{id} ─────────────────────��──────────────────────────
    // Actualiza los datos de un usuario existente

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User user) throws BadRequestException {
		return ResponseEntity.ok(userService.update(id, user));
    }

    // ── DELETE /api/v1/users/{id} ─────────────────────────────────────────────
    // Elimina un usuario por su ID

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}