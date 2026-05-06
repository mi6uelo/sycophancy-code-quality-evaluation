package com.example.sycophancy.service;

import com.example.sycophancy.model.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Usuario save(Usuario usuario);
    Usuario update(Usuario usuario);
    void deleteById(Long id);
}