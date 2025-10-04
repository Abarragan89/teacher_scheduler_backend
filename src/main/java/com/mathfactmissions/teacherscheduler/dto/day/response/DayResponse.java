package com.mathfactmissions.teacherscheduler.dto.day.response;

import com.mathfactmissions.teacherscheduler.dto.schedule.response.ScheduleResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record DayResponse(
        UUID id,
        LocalDate dayDate,
        List<ScheduleResponse> schedules
) {}
