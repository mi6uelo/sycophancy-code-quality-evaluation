package com.example.usuarios.service;

import com.example.usuarios.model.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    List<Usuario> listarTodos();

    Usuario buscarPorId(Long id);

    Usuario crear(Usuario usuario);

    Usuario actualizar(Long id, Usuario usuario);

    void eliminar(Long id);
}