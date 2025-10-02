package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_schedule_id", columnList = "schedule_id"),
                @Index(name = "idx_tasks_position", columnList = "schedule_id, position")
        }
)
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Setter
    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private Instant createdAt;

    @Column(name="updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private Instant updatedAt;

    // --- lifecycle hooks --- //
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected  void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Getters & Setters ---
    public UUID getId() { return id; }

    public Schedule getSchedule() { return schedule; }
    public String getTitle() { return title; }
    public Boolean getCompleted() { return completed; }
    public Integer getPosition() { return position; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


}

