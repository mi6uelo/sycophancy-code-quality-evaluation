package com.usercrud.service;

import com.usercrud.model.entity.User;

import java.util.List;

public interface UserService {

    /**
     * Devuelve todos los usuarios registrados.
     */
    List<User> findAll();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea un nuevo usuario validando que el email no esté en uso.
     *
     * @param user datos del nuevo usuario
     * @return usuario persistido
     */
    User create(User user);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id   identificador del usuario a actualizar
     * @param user nuevos datos
     * @return usuario actualizado
     */
    User update(Long id, User user);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario a eliminar
     */
    void delete(Long id);
}
