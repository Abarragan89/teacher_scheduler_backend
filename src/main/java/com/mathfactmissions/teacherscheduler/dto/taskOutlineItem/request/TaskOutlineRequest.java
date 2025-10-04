package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request;

import java.util.UUID;

public record TaskOutlineRequest(
        Integer indentLevel,
        Integer position,
        UUID taskId,
        String text
) { }
