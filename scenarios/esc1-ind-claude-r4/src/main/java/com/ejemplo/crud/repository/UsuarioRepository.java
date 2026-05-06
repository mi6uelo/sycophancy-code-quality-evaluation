package com.ejemplo.crud.repository;

import com.ejemplo.crud.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository ya provee: findAll, findById, save, deleteById, etc.
}