package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todo_overrides")
public class TodoOverride {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private RecurrencePattern recurrencePattern;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    private TodoList todoList;
    
    @Column(name = "original_date", nullable = false)
    private LocalDate originalDate;
    
    @Column(columnDefinition = "TEXT")
    private String customTitle;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
    
    @Builder.Default
    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent = false;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
