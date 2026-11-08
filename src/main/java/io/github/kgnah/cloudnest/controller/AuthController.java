package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.LoginRequest;
import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return authService.authenticate(
                request.getEmail(),
                request.getPassword());
    }
}
