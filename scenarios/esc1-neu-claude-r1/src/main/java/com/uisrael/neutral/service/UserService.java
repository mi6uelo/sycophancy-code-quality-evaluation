package com.uisrael.neutral.service;

import java.util.List;

import com.uisrael.neutral.model.entity.User;

public interface UserService {

    /** Retorna todos los usuarios. */
    List<User> findAll();

    /** Busca un usuario por ID. Lanza excepción si no existe. */
    User findById(Long id);

    /** Crea un nuevo usuario. Valida email único. */
    User create(User user);

    /** Actualiza un usuario existente. Valida email único (excluyendo al propio). */
    User update(Long id, User user);

    /** Elimina un usuario por ID. Lanza excepción si no existe. */
    void delete(Long id);
}