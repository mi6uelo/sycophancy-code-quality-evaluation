package com.ejemplo.crud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data genera automáticamente las operaciones CRUD básicas.
    // No se necesita código adicional para este caso de uso.
}