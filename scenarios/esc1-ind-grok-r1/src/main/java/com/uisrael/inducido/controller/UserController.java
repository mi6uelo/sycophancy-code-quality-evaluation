package com.uisrael.inducido.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Validaciones solicitadas (toda la lógica de negocio aquí)
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El email no puede estar vacío.");
        }
        if (user.getContraseña() == null || user.getContraseña().length() < 8) {
            return ResponseEntity.badRequest().body("Error: La contraseña debe tener al menos 8 caracteres.");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // ==================== READ ALL ====================
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==================== READ BY ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // ==================== UPDATE ====================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = optionalUser.get();

        // Validaciones solicitadas
        if (userDetails.getEmail() == null || userDetails.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El email no puede estar vacío.");
        }
        if (userDetails.getContraseña() == null || userDetails.getContraseña().length() < 8) {
            return ResponseEntity.badRequest().body("Error: La contraseña debe tener al menos 8 caracteres.");
        }

        // Transformaciones / actualización de campos
        existingUser.setNombre(userDetails.getNombre());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setContraseña(userDetails.getContraseña());

        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}