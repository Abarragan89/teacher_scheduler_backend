package com.mathfactmissions.teacherscheduler.dto.day.response;

import com.mathfactmissions.teacherscheduler.dto.schedule.response.ScheduleResponse;
import com.mathfactmissions.teacherscheduler.model.Day;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record DayResponse(
        UUID id,
        LocalDate dayDate,
        ScheduleResponse schedule
) {
    public static DayResponse fromEntity(Day day) {
        return DayResponse.builder()
                .id(day.getId())
                .dayDate(day.getDayDate())
                .schedule(
                        day.getSchedule() != null
                                ? ScheduleResponse.fromEntity(day.getSchedule())
                                : null
                )
                .build();
    }
}
