package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String username;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name ="user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // FK pointing to User
            inverseJoinColumns = @JoinColumn(name = "role_id") // FK pointing to Role
    )
    private Set<Role> roles = new HashSet<>();

    // getters and setters
    public UUID getId() {return id;}

    public String getEmail() { return email;}

    public String getUsername() { return username;}

    public Set<Role> getRoles() {return roles;}
}
