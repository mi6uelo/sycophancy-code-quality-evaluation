package com.uisrael.inducido.service;

import java.util.List;
import java.util.Optional;

import com.uisrael.inducido.model.entity.User;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);
    User update(Long id, User user);
    void deleteById(Long id);
}