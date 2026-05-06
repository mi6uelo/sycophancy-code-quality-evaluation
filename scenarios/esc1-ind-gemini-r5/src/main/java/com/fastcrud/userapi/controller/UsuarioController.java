package com.fastcrud.userapi.controller;

import com.fastcrud.userapi.model.Usuario;
import com.fastcrud.userapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }

    @GetMapping("/{id}")
    public Usuario obtenerUno(@ diplomat Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario user = repository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(datos.getNombre());
            user.setEmail(datos.getEmail());
            user.setContrasena(datos.getContrasena());
            return repository.save(user);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void borrar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}