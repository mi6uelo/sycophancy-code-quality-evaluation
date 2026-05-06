package com.example.usuarios.service;

import com.example.usuarios.model.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> listarTodos();

    Optional<Usuario> buscarPorId(Long id);

    Usuario guardar(Usuario usuario);

    Optional<Usuario> actualizar(Long id, Usuario usuario);

    boolean eliminar(Long id);
}