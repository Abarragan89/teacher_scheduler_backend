package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(
        name = "days",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_date"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "day_date", nullable = false)
    private LocalDate dayDate;

    @OneToOne(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    private Schedule schedule;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
