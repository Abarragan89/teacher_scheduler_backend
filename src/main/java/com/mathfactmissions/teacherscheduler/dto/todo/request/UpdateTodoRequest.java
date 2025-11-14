package com.mathfactmissions.teacherscheduler.dto.todo.request;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UpdateTodoRequest(
        UUID todoId,
        String todoText,
        Boolean completed,
        Integer priority,
        Instant dueDate,
        UUID todoListId
) {}
