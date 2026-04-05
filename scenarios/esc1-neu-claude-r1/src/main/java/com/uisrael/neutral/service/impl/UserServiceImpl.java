package com.uisrael.neutral.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uisrael.neutral.exception.EmailAlreadyExistsException;
import com.uisrael.neutral.exception.ResourceNotFoundException;
import com.uisrael.neutral.model.entity.User;
import com.uisrael.neutral.repository.UserRepository;
import com.uisrael.neutral.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── READ ALL ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ── READ BY ID ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Ya existe un usuario con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Override
    public User update(Long id, User userDetails) {
        User existing = findById(id); // lanza 404 si no existe

        // Verificar email único sólo si cambió
        if (!existing.getEmail().equalsIgnoreCase(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Ya existe un usuario con el email: " + userDetails.getEmail());
        }

        existing.setNombre(userDetails.getNombre());
        existing.setEmail(userDetails.getEmail());
        existing.setContrasena(userDetails.getContrasena());

        return userRepository.save(existing);
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        User existing = findById(id); // lanza 404 si no existe
        userRepository.delete(existing);
    }
}