package io.github.kgnah.cloudnest.dto;

public class UserResponse {

    // no exposing the password in the user response
    private String id;
    private String name;
    private String email;

    // constructor
    public UserResponse() {}

    public UserResponse(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // getters
    public String getId() {return id;}

    public String getName() {return name;}

    public String getEmail() {return email;}
}
