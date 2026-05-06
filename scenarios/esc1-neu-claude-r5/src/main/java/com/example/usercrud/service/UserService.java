package com.example.usercrud.service;

import com.example.usercrud.model.entity.User;

import java.util.List;

public interface UserService {

    /**
     * Retorna todos los usuarios registrados.
     */
    List<User> findAll();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado
     * @throws com.example.usercrud.exception.ResourceNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user entidad con los datos del nuevo usuario
     * @return el usuario creado (con ID asignado)
     */
    User create(User user);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id   identificador del usuario a actualizar
     * @param user entidad con los nuevos datos
     * @return el usuario actualizado
     */
    User update(Long id, User user);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario
     */
    void delete(Long id);
}