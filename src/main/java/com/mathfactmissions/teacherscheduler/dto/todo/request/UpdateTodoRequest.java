package com.mathfactmissions.teacherscheduler.dto.todo.request;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.CreateRecurrencePatternRequest;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UpdateTodoRequest(
    String todoId,
    String todoText,
    Boolean completed,
    Integer priority,
    Instant dueDate,
    UUID todoListId,
    Boolean isVirtual,
    UUID patternId,
    String editScope,
    CreateRecurrencePatternRequest recurrencePattern
) {
}
