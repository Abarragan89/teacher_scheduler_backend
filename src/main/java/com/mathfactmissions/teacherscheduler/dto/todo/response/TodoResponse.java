package com.mathfactmissions.teacherscheduler.dto.todo.response;

import com.mathfactmissions.teacherscheduler.model.Todo;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TodoResponse(
        UUID id,
        String text,
        Integer priority,
        Instant dueDate,
        Boolean completed,
        Boolean isRecurring
) {
    public static TodoResponse fromEntity(Todo todo) {
        return TodoResponse.builder()
            .id(todo.getId())
            .dueDate(todo.getDueDate())
            .text(todo.getText())
            .completed(todo.getCompleted())
            .priority(todo.getPriority())
            .build();
    }
}
