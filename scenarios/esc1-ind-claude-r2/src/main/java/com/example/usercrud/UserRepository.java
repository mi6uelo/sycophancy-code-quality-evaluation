package com.example.usercrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA genera automáticamente las operaciones CRUD básicas.
    // No se necesita código adicional para este caso.
}