package com.mathfactmissions.teacherscheduler.dto.recurringTodos.response;

import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record RecurrencePatternResponse(
    UUID id,
    String type,              // DAILY, WEEKLY, MONTHLY, YEARLY
    String text,
    List<Integer> daysOfWeek,        // ex: [1, 3, 5]
    List<Integer> daysOfMonth,       // ex: [1, 15, 30]
    String monthPatternType,  // ex: "DAY_OF_MONTH" or "NTH_WEEKDAY"
    NthWeekdayOccurrence nthWeekdayOccurrence,
    Integer yearlyMonth,
    Integer yearlyDay,
    LocalTime timeOfDay,
    LocalDate startDate,
    LocalDate endDate,
    ZoneId timeZone
) {
    
    public static RecurrencePatternResponse fromEntity(RecurrencePattern rp) {
        if (rp == null) return null;
        
        return RecurrencePatternResponse.builder()
            .id(rp.getId())
            .type(rp.getType().name())
            .text(rp.getText())
            .daysOfWeek(parseCsvToIntList(rp.getDaysOfWeek()))
            .daysOfMonth(parseCsvToIntList(rp.getDaysOfMonth()))
            .monthPatternType(rp.getMonthPatternType() != null ? rp.getMonthPatternType().name() : null)
            .nthWeekdayOccurrence(
                rp.getNthWeekdayOccurrence() != null && rp.getNthWeekdayDay() != null
                    ? NthWeekdayOccurrence.fromEntity(rp)
                    : null
            )
            .yearlyMonth(rp.getYearlyMonth())
            .startDate(rp.getStartDate())
            .endDate(rp.getEndDate())
            .yearlyDay(rp.getYearlyDay())
            .timeOfDay(rp.getTimeOfDay())
            .timeZone(rp.getTimeZone())
            .build();
    }
    
    private static List<Integer> parseCsvToIntList(String csv) {
        if (csv == null || csv.isBlank()) return null;
        
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
    
    @Builder
    public record NthWeekdayOccurrence(Integer ordinal, Integer weekday) {
        
        public static NthWeekdayOccurrence fromEntity(RecurrencePattern rp) {
            return NthWeekdayOccurrence.builder()
                .weekday(rp.getNthWeekdayDay())
                .ordinal(rp.getNthWeekdayOccurrence())
                .build();
        }
        
    }
}
