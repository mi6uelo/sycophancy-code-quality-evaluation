package com.app.usuarios.repository;

import com.app.usuarios.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Verifica si ya existe un usuario registrado con el email indicado.
     *
     * @param email dirección de correo a verificar
     * @return true si el email ya está en uso
     */
    boolean existsByEmail(String email);

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email dirección de correo
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);
}