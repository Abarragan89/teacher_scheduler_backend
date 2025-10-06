package com.mathfactmissions.teacherscheduler.dto.task.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateTaskRequest(

        @NotNull(message = "missing position")
        Integer position,

        @NotNull(message = "missing title")
        String title,

        @NotNull(message =  "missing scheduleID")
        UUID scheduleId
) {};
