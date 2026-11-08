package io.github.kgnah.cloudnest.repository;

import io.github.kgnah.cloudnest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{

    // email existence checking -> prevent duplicate email
    boolean existsByEmail(String email);

    // find user by email, edge case: not exist email
    Optional<User> findByEmail(String email);
}
