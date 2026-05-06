package com.example.usercrud.repository;

import com.example.usercrud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email dirección de correo electrónico
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email indicado.
     *
     * @param email dirección de correo electrónico
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}