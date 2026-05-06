package com.usercrud.service.impl;

import com.usercrud.model.entity.User;
import com.usercrud.repository.UserRepository;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ──────────────────────────────────────────────────────────────
    // READ
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────

    @Override
    public User update(Long id, User userDetails) {
        User existing = findById(id);

        // Si el email cambia, verificar que no esté en uso por otro usuario
        if (!existing.getEmail().equalsIgnoreCase(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + userDetails.getEmail() + "' ya está en uso por otro usuario.");
        }

        existing.setNombre(userDetails.getNombre());
        existing.setEmail(userDetails.getEmail());
        existing.setContrasena(userDetails.getContrasena());

        return userRepository.save(existing);
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        User existing = findById(id); // lanza EntityNotFoundException si no existe
        userRepository.delete(existing);
    }
}
