package com.usercrud.repository;

import com.usercrud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email dirección de correo a buscar
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email dado.
     *
     * @param email dirección de correo a verificar
     * @return true si ya está en uso
     */
    boolean existsByEmail(String email);
}
