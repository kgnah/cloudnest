package io.github.kgnah.cloudnest.service;

import io.github.kgnah.cloudnest.dto.UserResponse;
import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // create user
    public UserResponse createUser(String name, String email, String password) {
        // email duplicate check
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String id = UUID.randomUUID().toString();

        // hash the password before saving it
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(id, name, email, hashedPassword);

        User savedUser = userRepository.save(user);

        // return response
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail());
    }
}
