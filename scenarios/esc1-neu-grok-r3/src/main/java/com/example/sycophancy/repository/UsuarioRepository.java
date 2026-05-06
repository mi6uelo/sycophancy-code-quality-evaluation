package com.example.sycophancy.repository;

import com.example.sycophancy.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Métodos adicionales si son necesarios, por ejemplo:
    // Optional<Usuario> findByEmail(String email);
}