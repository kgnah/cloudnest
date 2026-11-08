package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.UserResponse;
import io.github.kgnah.cloudnest.dto.UserRequest;
import io.github.kgnah.cloudnest.entity.User;
import io.github.kgnah.cloudnest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // constructor
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // create user
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword());
    }


}
