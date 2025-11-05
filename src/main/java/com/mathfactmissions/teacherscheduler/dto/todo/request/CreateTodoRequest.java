package com.mathfactmissions.teacherscheduler.dto.todo.request;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateTodoRequest(
        UUID todoListId,
        String todoText,
        UUID todoId,
        Integer priority,
        Instant dueDate

) {
}
