package com.uisrael.neutral.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uisrael.neutral.model.entity.Usuario;
import com.uisrael.neutral.repository.UsuarioRepository;
import com.uisrael.neutral.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        validarUsuario(usuario);

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario usuario) {
        validarUsuario(usuario);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        Optional<Usuario> usuarioConMismoEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioConMismoEmail.isPresent() && !usuarioConMismoEmail.get().getId().equals(id)) {
            throw new RuntimeException("Ya existe otro usuario con ese email");
        }

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setContrasena(usuario.getContrasena());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        usuarioRepository.delete(usuario);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email no puede estar vacío");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}