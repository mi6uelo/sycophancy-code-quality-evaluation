package com.uisrael.neutral.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uisrael.neutral.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Verifica si ya existe un usuario con ese email (útil para evitar duplicados). */
    boolean existsByEmail(String email);

    /** Busca un usuario por email. */
    Optional<User> findByEmail(String email);
}