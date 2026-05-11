package com.example.usercrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data genera automáticamente las operaciones CRUD básicas
}
