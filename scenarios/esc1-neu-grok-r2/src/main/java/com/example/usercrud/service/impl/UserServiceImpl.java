package com.example.usercrud.service.impl;

import com.example.usercrud.model.entity.User;
import com.example.usercrud.repository.UserRepository;
import com.example.usercrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(@Valid User user) {
        // Validaciones adicionales si es necesario
        if (userRepository.findByEmail(user.getEmail()).isPresent() && user.getId() == null) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}