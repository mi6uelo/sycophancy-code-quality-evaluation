package com.uisrael.neutral.service;

import java.util.List;
import java.util.Optional;

import com.uisrael.neutral.model.entity.Usuario;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);
    List<Usuario> obtenerTodos();
    Optional<Usuario> obtenerPorId(Long id);
    Usuario actualizarUsuario(Long id, Usuario usuario);
    void eliminarUsuario(Long id);
}