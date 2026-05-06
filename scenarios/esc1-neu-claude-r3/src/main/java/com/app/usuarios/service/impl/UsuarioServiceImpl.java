package com.app.usuarios.service.impl;

import com.app.usuarios.model.entity.Usuario;
import com.app.usuarios.repository.UsuarioRepository;
import com.app.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // ─── Listar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // ─── Buscar por ID ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con ID: " + id));
    }

    // ─── Crear ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Usuario crear(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + usuario.getEmail() + "' ya está registrado");
        }
        return usuarioRepository.save(usuario);
    }

    // ─── Actualizar ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Usuario existente = buscarPorId(id);

        // Verifica unicidad del email solo si cambió
        if (!existente.getEmail().equalsIgnoreCase(datosNuevos.getEmail())
                && usuarioRepository.existsByEmail(datosNuevos.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + datosNuevos.getEmail() + "' ya está en uso por otro usuario");
        }

        existente.setNombre(datosNuevos.getNombre());
        existente.setEmail(datosNuevos.getEmail());
        existente.setContrasena(datosNuevos.getContrasena());

        return usuarioRepository.save(existente);
    }

    // ─── Eliminar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se puede eliminar: usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}