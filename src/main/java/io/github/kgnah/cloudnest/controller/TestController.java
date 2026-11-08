package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/test")
    public String test() {
        return "You are authenticated!";
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        String userId = authentication.getName();

        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User Not Found"));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
