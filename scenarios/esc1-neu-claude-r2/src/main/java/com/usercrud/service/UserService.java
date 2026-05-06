package com.usercrud.service;

import com.usercrud.model.entity.User;

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
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea y persiste un nuevo usuario.
     *
     * @param user entidad a crear
     * @return el usuario creado con ID asignado
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
     * @param id identificador del usuario a eliminar
     */
    void delete(Long id);
}
