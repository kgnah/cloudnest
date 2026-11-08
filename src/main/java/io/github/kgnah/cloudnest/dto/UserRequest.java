package io.github.kgnah.cloudnest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    // constructor
    public UserRequest() {}

    // getters & setters
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

}
