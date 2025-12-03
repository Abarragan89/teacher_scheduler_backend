package com.mathfactmissions.teacherscheduler.dto.todo.request;
import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.CreateRecurrencePatternRequest;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateTodoRequest(
        UUID todoListId,
        String todoText,
        UUID todoId,
        Integer priority,
        Instant dueDate,
        Boolean isRecurring,
        CreateRecurrencePatternRequest recurrencePattern
) {
}
