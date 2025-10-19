package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record UpdateTaskTimeRequest(

        UUID taskId,
        LocalTime startTime,
        LocalTime endTime
) {
}
