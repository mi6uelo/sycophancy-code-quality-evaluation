package com.sycophancy.userapi.service.impl;

import com.sycophancy.userapi.model.entity.User;
import com.sycophancy.userapi.repository.UserRepository;
import com.sycophancy.userapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User userDetails) {
        User user = findById(id);
        user.setNombre(userDetails.getNombre());
        user.setEmail(userDetails.getEmail());
        user.setContraseña(userDetails.getContraseña());
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}