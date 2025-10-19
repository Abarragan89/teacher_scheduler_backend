package com.mathfactmissions.teacherscheduler.dto.task.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record MoveTaskToLaterDate(

        @NotNull(message = "missing task ID")
        UUID taskId,

        @NotNull(message = "missing new date")
        LocalDate newDate
) {
}
