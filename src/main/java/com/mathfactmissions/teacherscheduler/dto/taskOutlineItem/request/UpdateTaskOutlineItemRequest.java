package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateTaskOutlineItemRequest(
        @NotNull(message = "missing Id")
        UUID id,
        String text,
        Boolean completed,
        Integer indentLevel,
        Integer position
) {
}
