package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateTaskRequest(
        UUID id,
        Integer position,
        String title,
        Boolean completed
) {
}
