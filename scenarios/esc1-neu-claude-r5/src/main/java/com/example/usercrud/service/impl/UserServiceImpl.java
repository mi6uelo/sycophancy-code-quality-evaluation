package com.example.usercrud.service.impl;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.usercrud.exception.ResourceNotFoundException;
import com.example.usercrud.model.entity.User;
import com.example.usercrud.repository.UserRepository;
import com.example.usercrud.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ── Listar todos ──────────────────────��──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ── Buscar por ID ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User create(User user) throws BadRequestException {
        try {
			if (userRepository.existsByEmail(user.getEmail())) {
			    throw new BadRequestException(
			            "Ya existe un usuario registrado con el email: " + user.getEmail());
			}
		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return userRepository.save(user);
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User update(Long id, User userDetails) throws BadRequestException {
        User existing = findById(id);

        // Verificar si el nuevo email ya está en uso por OTRO usuario
        if (!existing.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new BadRequestException(
                    "El email '" + userDetails.getEmail() + "' ya está en uso por otro usuario.");
        }

        existing.setNombre(userDetails.getNombre());
        existing.setEmail(userDetails.getEmail());
        existing.setContrasena(userDetails.getContrasena());

        return userRepository.save(existing);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        User existing = findById(id);
        userRepository.delete(existing);
    }
}