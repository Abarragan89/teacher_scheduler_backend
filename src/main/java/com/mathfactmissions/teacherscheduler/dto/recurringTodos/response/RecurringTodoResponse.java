package com.mathfactmissions.teacherscheduler.dto.recurringTodos.response;

import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import lombok.Builder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record RecurringTodoResponse(
        UUID id,
        String text,
        TodoList todoList,
        String recurrenceType,
        LocalTime timeOfDay,
        LocalDate lastGeneratedDate,
        Instant createdAt,
        Instant updatedAt,
        String patternDescription,
        RecurrencePatternDetails patternDetails
) {}
