package com.mathfactmissions.teacherscheduler.dto.schedule.request;

import java.time.LocalDate;
import java.util.UUID;

public record MoveScheduleRequest(
        LocalDate dayDate,
        UUID scheduleId
) {
}
