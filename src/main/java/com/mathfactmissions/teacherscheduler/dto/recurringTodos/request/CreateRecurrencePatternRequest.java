package com.mathfactmissions.teacherscheduler.dto.recurringTodos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record CreateRecurrencePatternRequest(

        @NotBlank(message = "Recurrence type is required") String recurrenceType,
        @NotBlank(message = "Time is required") String time,
        List<String> selectedDays,
        String monthPatternType,
        List<String> selectedMonthDays,
        NthWeekday nthWeekday,
        LocalDate yearlyDate
) {
    public record NthWeekday(Integer nth, Integer weekday) {}

}
