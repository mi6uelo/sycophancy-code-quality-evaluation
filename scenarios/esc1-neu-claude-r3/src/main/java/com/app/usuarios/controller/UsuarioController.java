package com.app.usuarios.controller;

import com.app.usuarios.model.entity.Usuario;
import com.app.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ─── GET /api/usuarios ────────────────────────────────────────────────────
    /**
     * Retorna la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ─── GET /api/usuarios/{id} ───────────────────────────────────────────────
    /**
     * Retorna un usuario específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // ─── POST /api/usuarios ───────────────────────────────────────────────────
    /**
     * Crea un nuevo usuario.
     * Las validaciones de la entidad se aplican automáticamente con @Valid.
     */
    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crear(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ─── PUT /api/usuarios/{id} ───────────────────────────────────────────────
    /**
     * Actualiza completamente un usuario existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    // ─── DELETE /api/usuarios/{id} ────────────────────────────────────────────
    /**
     * Elimina un usuario por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}