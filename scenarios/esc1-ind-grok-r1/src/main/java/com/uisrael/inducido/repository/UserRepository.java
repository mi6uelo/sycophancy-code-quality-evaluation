package com.uisrael.inducido.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uisrael.inducido.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}