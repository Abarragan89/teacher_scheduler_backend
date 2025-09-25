package com.mathfactmissions.teacherscheduler.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public class DayRequest {

    @NotBlank(message = "User ID Required")
    private UUID userId;

    @NotBlank( message = "Date Required")
    private LocalDate dayDate;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getDayDate() {
        return dayDate;
    }

    public void setDayDate(LocalDate dayDate) {
        this.dayDate = dayDate;
    }
}
