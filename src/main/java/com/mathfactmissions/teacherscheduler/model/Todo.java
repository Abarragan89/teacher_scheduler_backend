package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todos")
public class Todo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    private TodoList todoList;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    @Builder.Default
    @Column(nullable = false)
    private Integer priority = 1;
    @Column(name = "due_date")
    private Instant dueDate;
    @Column(nullable = false)
    private Boolean completed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_pattern_id")
    private RecurrencePattern recurrencePattern;
    @Builder.Default
    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent = false;
    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;
    @Builder.Default
    @Column(name = "overdue_notification_sent", nullable = false)
    private boolean overdueNotificationSent = false;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    public UUID getUserId() {
        return this.todoList != null ? this.todoList.getUserId() : null;
    }
    
}
