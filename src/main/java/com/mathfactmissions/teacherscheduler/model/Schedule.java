package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = "day_id")
)
public class Schedule {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @Setter
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<Task> tasks = new HashSet<Task>();

    @Column (name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    @PrePersist
    protected  void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected  void onUpdate() {
        this.updatedAt = Instant.now();
    }


}

