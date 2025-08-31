package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    // getters and setters
    public Long getId() {return id;}

    public String getEmail() { return email;}
    public void setEmail(String email) {this.email = email;}

    public void setUsername(String username) { this.username = username;}
    public String getUsername() { return username;}

    public void setPassword(String password) { this.password = password;}
    public String getPassword() { return password;}
}
