package com.uisrael.neutral.service;

import java.util.List;

import com.uisrael.neutral.model.entity.Usuario;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Usuario guardar(Usuario usuario);
    Usuario buscarPorId(Long id);
    void eliminar(Long id);
}