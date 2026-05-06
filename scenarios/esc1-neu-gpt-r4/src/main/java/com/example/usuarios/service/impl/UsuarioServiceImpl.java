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
    public Usuario crear(Usuario usuario) {
        validarEmailDuplicado(usuario.getEmail(), null);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        validarEmailDuplicado(usuario.getEmail(), id);

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setContrasena(usuario.getContrasena());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }

        usuarioRepository.deleteById(id);
    }

    private void validarEmailDuplicado(String email, Long idActual) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            if (idActual == null || !usuario.getId().equals(idActual)) {
                throw new RuntimeException("Ya existe un usuario registrado con el email: " + email);
            }
        });
    }
}