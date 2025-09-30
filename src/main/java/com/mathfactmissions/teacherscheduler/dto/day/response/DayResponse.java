package com.mathfactmissions.teacherscheduler.dto.day.response;

import java.time.LocalDate;
import java.util.UUID;

public class DayResponse {

    private UUID id;
    private LocalDate dayDate;

    public DayResponse(UUID id, LocalDate dayDate) {
        this.id = id;
        this.dayDate = dayDate;
    }

    public UUID getId(){
        return id;
    }

    public LocalDate getDayDate() {
        return dayDate;
    }

}
