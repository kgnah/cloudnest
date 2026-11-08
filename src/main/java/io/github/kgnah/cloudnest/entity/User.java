package io.github.kgnah.cloudnest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // use this to fix the PostgreSQL issue, as it doesn't allow to create "user" table
public class User {

    @Id
    private String id;

    private String name;
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    // constructors
    public User() {}

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // getters
    public String getId() {return id;}

    public String getName() {return name;}

    public String getEmail() {return email;}

    public String getPassword() {return password;}
}
