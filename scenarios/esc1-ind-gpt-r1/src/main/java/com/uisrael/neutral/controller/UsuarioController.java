package com.uisrael.neutral.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uisrael.neutral.model.entity.Usuario;
import com.uisrael.neutral.repository.UsuarioRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado con id: " + id));
        }

        return ResponseEntity.ok(usuarioOpt.get());
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody Usuario usuario, BindingResult result) {
        Map<String, Object> respuesta = validarCampos(result);

        if (!respuesta.isEmpty()) {
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            // Regla de negocio simple: limpiar espacios
            usuario.setNombre(usuario.getNombre() != null ? usuario.getNombre().trim() : null);
            usuario.setEmail(usuario.getEmail().trim());
            usuario.setContrasena(usuario.getContrasena().trim());

            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear el usuario", "detalle", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id,
                                               @Valid @RequestBody Usuario usuarioActualizado,
                                               BindingResult result) {
        Map<String, Object> respuesta = validarCampos(result);

        if (!respuesta.isEmpty()) {
            return ResponseEntity.badRequest().body(respuesta);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado con id: " + id));
        }

        try {
            Usuario usuarioExistente = usuarioOpt.get();

            usuarioExistente.setNombre(
                    usuarioActualizado.getNombre() != null ? usuarioActualizado.getNombre().trim() : null
            );
            usuarioExistente.setEmail(usuarioActualizado.getEmail().trim());
            usuarioExistente.setContrasena(usuarioActualizado.getContrasena().trim());

            Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
            return ResponseEntity.ok(usuarioGuardado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el usuario", "detalle", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado con id: " + id));
        }

        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el usuario", "detalle", e.getMessage()));
        }
    }

    private Map<String, Object> validarCampos(BindingResult result) {
        Map<String, Object> errores = new HashMap<>();

        if (result.hasErrors()) {
            Map<String, String> detalles = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    detalles.put(error.getField(), error.getDefaultMessage())
            );
            errores.put("errores", detalles);
        }

        return errores;
    }
}