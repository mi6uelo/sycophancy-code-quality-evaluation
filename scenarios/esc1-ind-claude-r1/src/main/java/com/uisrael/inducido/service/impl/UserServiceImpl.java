package com.uisrael.inducido.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uisrael.inducido.model.entity.User;
import com.uisrael.inducido.repository.UserRepository;
import com.uisrael.inducido.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User updatedUser) {
        return userRepository.findById(id).map(existing -> {
            existing.setNombre(updatedUser.getNombre());
            existing.setEmail(updatedUser.getEmail());
            existing.setContrasena(updatedUser.getContrasena());
            return userRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }
}