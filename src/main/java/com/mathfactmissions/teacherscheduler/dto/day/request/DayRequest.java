package com.mathfactmissions.teacherscheduler.dto.day.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class DayRequest {

    @NotNull( message = "Date Required")
    private LocalDate dayDate;


    public LocalDate getDayDate() {
        return dayDate;
    }

    public void setDayDate(LocalDate dayDate) {
        this.dayDate = dayDate;
    }
}
