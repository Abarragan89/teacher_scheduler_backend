package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RecurrenceEngine {
    
    /**
     * Calculate all occurrences between start and end dates
     */
    public List<LocalDate> calculateOccurrences(
        RecurrencePattern pattern,
        LocalDate start,
        LocalDate end
    ) {
        // Enforce pattern boundaries
        LocalDate effectiveStart =
            start.isBefore(pattern.getStartDate())
                ? pattern.getStartDate()
                : start;
        
        LocalDate effectiveEnd =
            pattern.getEndDate() != null && pattern.getEndDate().isBefore(end)
                ? pattern.getEndDate()
                : end;
        
        if (effectiveEnd.isBefore(effectiveStart)) {
            return List.of();
        }
        
        List<LocalDate> occurrences = new ArrayList<>();
        
        switch (pattern.getType()) {
            case DAILY -> occurrences.addAll(
                calculateDailyOccurrences(pattern, effectiveStart, effectiveEnd)
            );
            case WEEKLY -> occurrences.addAll(
                calculateWeeklyOccurrences(pattern, effectiveStart, effectiveEnd)
            );
            case MONTHLY -> occurrences.addAll(
                calculateMonthlyOccurrences(pattern, effectiveStart, effectiveEnd)
            );
            case YEARLY -> occurrences.addAll(
                calculateYearlyOccurrences(pattern, effectiveStart, effectiveEnd)
            );
        }
        
        return occurrences;
    }
    
    private List<LocalDate> calculateDailyOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        
        while (!start.isAfter(end)) {
            occurrences.add(start);
            start = start.plusDays(1);
        }
        
        return occurrences;
    }
    
    private List<LocalDate> calculateWeeklyOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        
        if (pattern.getDaysOfWeek() == null || pattern.getDaysOfWeek().isEmpty()) {
            return occurrences;
        }
        
        List<Integer> daysOfWeek = Arrays.stream(pattern.getDaysOfWeek().split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            int dayOfWeek = current.getDayOfWeek().getValue() % 7; // Convert to Sunday=0
            if (daysOfWeek.contains(dayOfWeek)) {
                occurrences.add(current);
            }
            current = current.plusDays(1);
        }
        
        return occurrences;
    }
    
    private List<LocalDate> calculateMonthlyOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        if (pattern.getMonthPatternType() == MonthPatternType.BY_DATE) {
            return calculateMonthlyByDate(pattern, start, end);
        } else {
            return calculateMonthlyByDay(pattern, start, end);
        }
    }
    
    private List<LocalDate> calculateMonthlyByDate(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        
        if (pattern.getDaysOfMonth() == null || pattern.getDaysOfMonth().isEmpty()) {
            return occurrences;
        }
        
        List<Integer> daysOfMonth = Arrays.stream(pattern.getDaysOfMonth().split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
        
        YearMonth currentMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        
        while (!currentMonth.isAfter(endMonth)) {
            for (Integer dayOfMonth : daysOfMonth) {
                LocalDate occurrence;
                
                if (dayOfMonth == -1) {
                    // Last day of month
                    occurrence = currentMonth.atEndOfMonth();
                } else {
                    // Specific day of month
                    if (dayOfMonth <= currentMonth.lengthOfMonth()) {
                        occurrence = currentMonth.atDay(dayOfMonth);
                    } else {
                        continue; // Skip if day doesn't exist in this month
                    }
                }
                
                if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                    occurrences.add(occurrence);
                }
            }
            currentMonth = currentMonth.plusMonths(1);
        }
        
        return occurrences;
    }
    
    private List<LocalDate> calculateMonthlyByDay(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        
        if (pattern.getNthWeekdayOccurrence() == null || pattern.getNthWeekdayDay() == null) {
            return occurrences;
        }
        
        int nthOccurrence = pattern.getNthWeekdayOccurrence();
        int weekday = pattern.getNthWeekdayDay();
        
        YearMonth currentMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        
        while (!currentMonth.isAfter(endMonth)) {
            LocalDate occurrence = findNthWeekdayInMonth(currentMonth, nthOccurrence, weekday);
            
            if (occurrence != null && !occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                occurrences.add(occurrence);
            }
            
            currentMonth = currentMonth.plusMonths(1);
        }
        
        return occurrences;
    }
    
    private LocalDate findNthWeekdayInMonth(YearMonth month, int nthOccurrence, int weekday) {
        try {
            if (nthOccurrence == -1) {
                // Last occurrence - use TemporalAdjusters
                DayOfWeek dayOfWeek = weekday == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(weekday);
                return month.atEndOfMonth().with(TemporalAdjusters.lastInMonth(dayOfWeek));
            }
            
            LocalDate firstOfMonth = month.atDay(1);
            int firstWeekday = firstOfMonth.getDayOfWeek().getValue() % 7;
            
            // Calculate days to add to get to the first occurrence of the weekday
            int daysToAdd = (weekday - firstWeekday + 7) % 7;
            
            // Add weeks for the nth occurrence
            daysToAdd += (nthOccurrence - 1) * 7;
            
            LocalDate nthWeekdayDate = firstOfMonth.plusDays(daysToAdd);
            
            // Check if it's still in the same month
            return nthWeekdayDate.getMonth() == month.getMonth() ? nthWeekdayDate : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<LocalDate> calculateYearlyOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        
        if (pattern.getYearlyMonth() == null || pattern.getYearlyDay() == null) {
            return occurrences;
        }
        
        int startYear = start.getYear();
        int endYear = end.getYear();
        
        for (int year = startYear; year <= endYear; year++) {
            try {
                LocalDate occurrence = LocalDate.of(year, pattern.getYearlyMonth(), pattern.getYearlyDay());
                
                if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                    occurrences.add(occurrence);
                }
            } catch (DateTimeException e) {
                // Invalid date (like Feb 29 in non-leap year)
                System.out.println("Error making year i think??>.. in recurrence engine");
            }
        }
        
        return occurrences;
    }
    
}
