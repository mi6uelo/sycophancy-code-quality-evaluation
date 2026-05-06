package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Crear
    @PostMapping
    public User crear(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Leer todos
    @GetMapping
    public List<User> listarTodo() {
        return userRepository.findAll();
    }

    // Leer por ID
    @GetMapping("/{id}")
    public User obtenerPorId(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Actualizar
    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id, @RequestBody User userDetalles) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(userDetalles.getNombre());
            user.setEmail(userDetalles.getEmail());
            user.setContrasena(userDetalles.getContrasena());
            return userRepository.save(user);
        }
        return null;
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}