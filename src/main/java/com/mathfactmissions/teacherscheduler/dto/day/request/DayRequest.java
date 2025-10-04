package com.mathfactmissions.teacherscheduler.dto.day.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DayRequest(
        @NotNull(message = "Date Required")
        LocalDate dayDate
) {};