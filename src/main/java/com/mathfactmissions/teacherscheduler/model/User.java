package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name ="user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // FK pointing to User
            inverseJoinColumns = @JoinColumn(name = "role_id") // FK pointing to Role
    )
    private Set<Role> roles = new HashSet<>();

    // getters and setters
    public Long getId() {return id;}

    public String getEmail() { return email;}
    public void setEmail(String email) {this.email = email;}

    public void setUsername(String username) { this.username = username;}
    public String getUsername() { return username;}

    public Set<Role> getRoles() {return roles;}
    public void setRoles(Set<Role> roles) {this.roles = roles;}
}
