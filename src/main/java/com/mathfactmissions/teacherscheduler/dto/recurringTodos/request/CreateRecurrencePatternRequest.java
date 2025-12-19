package com.mathfactmissions.teacherscheduler.dto.recurringTodos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Builder
public record CreateRecurrencePatternRequest(
    
    @NotBlank(message = "Recurrence type is required") String type,
    @NotBlank(message = "Time is required") String timeOfDay,
    @NotBlank(message = "Time Zone Required") ZoneId timeZone,
    List<String> daysOfWeek,
    String monthPatternType,
    List<String> daysOfMonth,
    NthWeekdayOccurrence nthWeekdayOccurrence,
    LocalDate yearlyDate,
    LocalDate startDate,
    LocalDate endDate

) {
    public record NthWeekdayOccurrence(Integer ordinal, Integer weekday) {
    }
    
}
