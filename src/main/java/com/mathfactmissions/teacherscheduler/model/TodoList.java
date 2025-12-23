package com.mathfactmissions.teacherscheduler.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "todo_lists",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
    }
)
public class TodoList {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Builder.Default
    @OneToMany(mappedBy = "todoList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("priority DESC")
    private List<Todo> todos = new ArrayList<Todo>();
    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = false;
    @Column(name = "list_name", nullable = false)
    private String listName;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    public UUID getUserId() {
        return this.user != null ? this.user.getId() : null;
    }
    
}
