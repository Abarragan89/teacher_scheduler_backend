package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table (name  = "roles")
public class Role {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Column(unique = true, nullable = false)
    private String name;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
