package com.usercrud.service.impl;

import com.usercrud.model.entity.User;
import com.usercrud.repository.UserRepository;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ─── READ ALL ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ─── READ ONE ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con ID: " + id));
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User update(Long id, User user) {
        User existing = findById(id);

        // Si el email cambia, verificar que el nuevo no esté en uso
        if (!existing.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "El email ya está en uso: " + user.getEmail());
        }

        existing.setNombre(user.getNombre());
        existing.setEmail(user.getEmail());
        existing.setContrasena(user.getContrasena());

        return userRepository.save(existing);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se puede eliminar: usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}