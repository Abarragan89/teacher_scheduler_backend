package com.mathfactmissions.teacherscheduler.dto.todo.response;

import com.mathfactmissions.teacherscheduler.model.Todo;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;


@Builder
public record RecurringTodoView(
UUID id,
String text,
Integer priority,
Instant dueDate,
Boolean completed,
Boolean isRecurring
) {
    public static RecurringTodoView fromEntity(Todo todo, Instant occurrenceTime) {
        return RecurringTodoView.builder()
        .id(todo.getId())
        .dueDate(occurrenceTime)
        .text(todo.getText())
        .completed(todo.getCompleted())
        .priority(todo.getPriority())
        .build();
    }
}
