package com.mathfactmissions.teacherscheduler.dto.recurringTodos.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RecurrencePatternDetails(
        UUID patternId,
        String type,
        String daysOfWeek,
        String monthPatternType,
        String daysOfMonth,
        Integer nthWeekdayOccurrence,
        Integer nthWeekdayDay,
        Integer yearlyMonth,
        Integer yearlyDay
) {}