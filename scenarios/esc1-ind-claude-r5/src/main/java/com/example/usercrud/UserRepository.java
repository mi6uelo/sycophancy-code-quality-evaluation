package com.example.usercrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA genera automáticamente las operaciones CRUD básicas.
    // No se requiere ninguna implementación adicional.
}