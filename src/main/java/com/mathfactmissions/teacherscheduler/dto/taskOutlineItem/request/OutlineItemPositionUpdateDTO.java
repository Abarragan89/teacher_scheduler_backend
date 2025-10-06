package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OutlineItemPositionUpdateDTO(
        UUID id,
        String text,
        Integer position,
        Integer indentLevel,
        Boolean completed,
        String taskId
) {}
