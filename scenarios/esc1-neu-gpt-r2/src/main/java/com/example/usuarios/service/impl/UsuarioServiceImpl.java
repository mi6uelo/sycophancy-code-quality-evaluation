package com.example.usuarios.service.impl;

import com.example.usuarios.model.entity.Usuario;
import com.example.usuarios.repository.UsuarioRepository;
import com.example.usuarios.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(usuarioExistente -> {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + usuario.getEmail());
        });

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con el id: " + id));

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setContrasena(usuario.getContrasena());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un usuario con el id: " + id);
        }

        usuarioRepository.deleteById(id);
    }
}