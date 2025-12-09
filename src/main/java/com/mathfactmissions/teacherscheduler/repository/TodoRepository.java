package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
    @Query("""
        SELECT t FROM Todo t 
        JOIN FETCH t.todoList tl
        WHERE t.dueDate BETWEEN :start AND :end
        AND t.notificationSent = false 
        AND t.completed = false
        ORDER BY t.dueDate ASC
    """)
    List<Todo> findTodosDueBetween(
    @Param("start") Instant start,
    @Param("end") Instant end
    );

    /**
     * Find overdue todos that haven't been notified yet
     */
    @Query("""
        SELECT t FROM Todo t 
        JOIN FETCH t.todoList tl
        WHERE t.dueDate < :now
        AND t.overdueNotificationSent = false 
        AND t.completed = false
        ORDER BY t.dueDate ASC
    """)
    List<Todo> findOverdueTodos(@Param("now") Instant now);
}
