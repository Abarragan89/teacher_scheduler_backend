package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// PushSubscription.java
@Entity
@Table(name = "push_subscriptions")
@Getter
@Setter
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false)
    private String p256dhKey;

    @Column(nullable = false)
    private String authKey;

    @Column(nullable = false)
    private UUID userId; // Link to your User entity

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public PushSubscription(String endpoint, String p256dhKey, String authKey, UUID userId) {
        this.endpoint = endpoint;
        this.p256dhKey = p256dhKey;
        this.authKey = authKey;
        this.userId = userId;
    }

    // Getters and Setters...
}
