package com.mathfactmissions.teacherscheduler.dto.schedule.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateScheduleRequest(
        @NotNull(message = "Missing day ID.")
        UUID dayId
) {}
