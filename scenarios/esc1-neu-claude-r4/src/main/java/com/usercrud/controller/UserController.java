package com.usercrud.controller;

import com.usercrud.model.entity.User;
import com.usercrud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── GET ALL ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ─── GET BY ID ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
