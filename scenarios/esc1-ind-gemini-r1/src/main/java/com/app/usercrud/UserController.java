package com.app.usercrud;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<User> listar() {
        return repository.findAll();
    }

    @PostMapping
    public User crear(@RequestBody User usuario) {
        return repository.save(usuario);
    }

    @GetMapping("/{id}")
    public User obtenerUno(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id, @RequestBody User userDetails) {
        User user = repository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(userDetails.getNombre());
            user.setEmail(userDetails.getEmail());
            user.setContraseña(userDetails.getContraseña());
            return repository.save(user);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}