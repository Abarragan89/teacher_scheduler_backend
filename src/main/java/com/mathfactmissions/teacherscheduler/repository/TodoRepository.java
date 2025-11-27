package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT t FROM Todo t WHERE t.isRecurring = true")
    List<Todo> findAllRecurringTodos();

//    @Query("SELECT COUNT(t) > 0 FROM Todo t WHERE t.recurringParentId = :parentId AND t.dueDate = :dueDate")
//    boolean existsByRecurringParentIdAndDueDate(@Param("parentId") UUID parentId, @Param("dueDate") Instant dueDate);
//
//    @Modifying
//    @Query("DELETE FROM Todo t WHERE t.recurringParentId = :parentId AND t.dueDate > :afterDate")
//    void deleteByRecurringParentIdAndDueDateAfter(@Param("parentId") UUID parentId, @Param("afterDate") Instant afterDate);


//    @Modifying
//    @Query("DELETE FROM Todo t WHERE t.recurringParent = :parent AND t.dueDate > :afterDate")
//    void deleteByRecurringParentAndDueDateAfter(@Param("parent") Todo parent, @Param("afterDate") Instant afterDate);

//    @Query("SELECT COUNT(t) > 0 FROM Todo t WHERE t.recurringParent = :parent AND t.dueDate = :dueDate")
//    boolean existsByRecurringParentAndDueDate(@Param("parent") Todo parent, @Param("dueDate") Instant dueDate);

//    List<Todo> findByUserAndIsRecurring(User user, Boolean isRecurring);

}
