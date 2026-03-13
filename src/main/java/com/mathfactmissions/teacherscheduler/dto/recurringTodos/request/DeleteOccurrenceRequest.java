package com.mathfactmissions.teacherscheduler.dto.recurringTodos.request;

import java.time.Instant;
import java.util.UUID;

public record DeleteOccurrenceRequest(
    String todoId,      // virtual_ prefix or real UUID
    UUID patternId,
    Instant dueDate     // to parse originalDate for virtual
) {
}
