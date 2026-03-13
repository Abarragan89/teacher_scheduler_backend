package com.mathfactmissions.teacherscheduler.dto.todo.response;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.response.RecurrencePatternResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record TodoResponse(
    String id,
    String text,
    Integer priority,
    Instant dueDate,
    LocalTime timeOfDay,    // new
    Boolean completed,
    Boolean isRecurring,
    Boolean isVirtual,
    UUID patternId,         // new
    RecurrencePatternResponse recurrencePattern,
    UUID todoListId,
    String listName
) {
    public static TodoResponse fromEntity(Todo todo) {
        return TodoResponse.builder()
            .id(todo.getId().toString())
            .dueDate(todo.getDueDate())
            .text(todo.getText())
            .completed(todo.getCompleted())
            .priority(todo.getPriority())
            .isRecurring(false)
            .isVirtual(false)
            .todoListId(todo.getTodoList().getId())
            .listName(todo.getTodoList().getListName())
            .recurrencePattern(null)
            .patternId(null)
            .timeOfDay(null)
            .build();
    }
}