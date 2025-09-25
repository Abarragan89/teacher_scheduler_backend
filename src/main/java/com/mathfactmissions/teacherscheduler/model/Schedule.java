package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    private String description;

    @Column(name ="user_id", nullable = false)
    private UUID userId;

}
