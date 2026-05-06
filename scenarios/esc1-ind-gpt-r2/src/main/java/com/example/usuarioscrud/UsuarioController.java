package com.example.usuarioscrud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario obtenerPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            return null;
        }

        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        usuario.setContrasena(datos.getContrasena());

        return usuarioRepository.save(usuario);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "Usuario eliminado correctamente";
    }
}