package com.example.sycophancy.service.impl;

import com.example.sycophancy.model.entity.Usuario;
import com.example.sycophancy.repository.UsuarioRepository;
import com.example.sycophancy.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario save(@Valid Usuario usuario) {
        // Aquí se pueden agregar validaciones adicionales si es necesario
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(@Valid Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new RuntimeException("Usuario no encontrado con id: " + usuario.getId());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}