package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskPositionUpdateDTO(
        UUID id,
        String title,
        Integer position,
        Boolean completed
) {}
