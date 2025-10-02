package com.mathfactmissions.teacherscheduler.dto.day.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DayRequest {

    @NotNull(message = "Date Required")
    private LocalDate dayDate;
}