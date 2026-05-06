package com.example.usuarios.service.impl;

import com.example.usuarios.exception.ResourceNotFoundException;
import com.example.usuarios.model.entity.Usuario;
import com.example.usuarios.repository.UsuarioRepository;
import com.example.usuarios.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    public Usuario crear(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + usuario.getEmail());
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = buscarPorId(id);

        if (!usuarioExistente.getEmail().equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe otro usuario registrado con el email: " + usuario.getEmail());
        }

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setContrasena(usuario.getContrasena());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminar(Long id) {
        Usuario usuarioExistente = buscarPorId(id);
        usuarioRepository.delete(usuarioExistente);
    }
}