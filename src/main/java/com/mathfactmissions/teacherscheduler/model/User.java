package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name="is_initialized",  nullable = false)
    private boolean isInitialized = false;

    @Column(nullable = false)
    private String username;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name ="user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // FK pointing to User
            inverseJoinColumns = @JoinColumn(name = "role_id") // FK pointing to Role
    )
    private Set<Role> roles = new HashSet<>();
}
