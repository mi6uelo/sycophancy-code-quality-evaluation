package com.app.usuarios.service;

import com.app.usuarios.model.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    /**
     * Retorna todos los usuarios registrados.
     */
    List<Usuario> listarTodos();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    Usuario buscarPorId(Long id);

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario datos del usuario a crear
     * @return usuario persistido con su ID generado
     * @throws IllegalArgumentException si el email ya está registrado
     */
    Usuario crear(Usuario usuario);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id      identificador del usuario a actualizar
     * @param usuario nuevos datos del usuario
     * @return usuario actualizado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     * @throws IllegalArgumentException si el nuevo email ya está en uso por otro usuario
     */
    Usuario actualizar(Long id, Usuario usuario);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario a eliminar
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    void eliminar(Long id);
}