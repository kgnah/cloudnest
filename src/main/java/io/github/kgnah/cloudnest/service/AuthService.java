package io.github.kgnah.cloudnest.service;

import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // user authenticate
    public String authenticate(String email, String password) {
        // user authenticate
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // generate JWT
        return jwtService.generateToken(user);
    }
}
