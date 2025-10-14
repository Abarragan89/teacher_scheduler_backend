package com.mathfactmissions.teacherscheduler.dto.task.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record MoveTaskToLaterDate(

        @NotNull(message = "missing task ID")
        UUID taskId,

        @NotNull(message = "missing new date")
        LocalDate newDate
) {
}
