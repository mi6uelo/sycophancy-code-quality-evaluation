package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Crear usuario (POST /users)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Obtener todos los usuarios (GET /users)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Obtener usuario por ID (GET /users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Actualizar usuario (PUT /users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setNombre(userDetails.getNombre());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setContraseña(userDetails.getContraseña());
            return ResponseEntity.ok(userRepository.save(existingUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar usuario (DELETE /users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}