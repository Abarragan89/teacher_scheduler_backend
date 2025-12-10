package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.RecurrencePatternRepository;
import com.mathfactmissions.teacherscheduler.enums.RecurrenceType;
import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurrencePatternService {

    private final RecurrencePatternRepository recurrencePatternRepository;
    private final UserService userService;
    private final TodoListRepository todoListRepository;

    @Transactional
    public RecurrencePattern createRecurrencePattern(CreateTodoRequest request, UUID userId) {

        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        TodoList todoList = todoListRepository
            .findByIdAndUser_Id(request.todoListId(), userId)
            .orElseThrow(() -> new RuntimeException("Todo list not found or not owned"));

        RecurrencePattern pattern = RecurrencePattern.builder()
            .type(RecurrenceType.valueOf(request.recurrencePattern().recurrenceType().toUpperCase()))
            .timeOfDay(LocalTime.parse(request.recurrencePattern().time()))
            .timeZone(request.recurrencePattern().timeZone())
            .startDate(request.recurrencePattern().startDate())
            .endDate(request.recurrencePattern().endDate())
            .user(user)
            .todoList(todoList)
            .build();

        // Set up the recurrence pattern rules based on patternType
        switch (pattern.getType()) {
            case WEEKLY:
                pattern.setDaysOfWeek(String.join(",", request.recurrencePattern().selectedDays()));
                break;
            case MONTHLY:
                if ("BY_DATE".equals(request.recurrencePattern().monthPatternType())) {
                    pattern.setMonthPatternType(MonthPatternType.BY_DATE);
                    pattern.setDaysOfMonth(
                    String.join(",", request.recurrencePattern().selectedMonthDays())
                    );
                } else {
                    pattern.setMonthPatternType(MonthPatternType.BY_DAY);
                    pattern.setNthWeekdayOccurrence(request.recurrencePattern().nthWeekday().nth());
                    pattern.setNthWeekdayDay(request.recurrencePattern().nthWeekday().weekday());
                }
                break;
            case YEARLY:
                LocalDate yearlyDate = request.recurrencePattern().yearlyDate();
                pattern.setYearlyMonth(yearlyDate.getMonthValue());
                pattern.setYearlyDay(yearlyDate.getDayOfMonth());
                break;
            case DAILY:
            default:
                break;
        }
        return recurrencePatternRepository.save(pattern);
    }


    /**
     * Calculate all occurrences between start and end dates
     */
    public List<LocalDate> calculateOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();

        switch (pattern.getType()) {
            case DAILY:
                occurrences.addAll(calculateDailyOccurrences(start, end));
                break;
            case WEEKLY:
                occurrences.addAll(calculateWeeklyOccurrences(pattern, start, end));
                break;
            case MONTHLY:
                occurrences.addAll(calculateMonthlyOccurrences(pattern, start, end));
                break;
            case YEARLY:
                occurrences.addAll(calculateYearlyOccurrences(pattern, start, end));
                break;
        }

        return occurrences.stream()
        .filter(date -> !date.isBefore(start) && !date.isAfter(end))
        .sorted()
        .collect(Collectors.toList());
    }

    private List<LocalDate> calculateDailyOccurrences(LocalDate start, LocalDate end) {
        List<LocalDate> occurrences = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            occurrences.add(current);
            current = current.plusDays(1);
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
            log.warn("Error calculating nth weekday for month {}: {}", month, e.getMessage());
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
                log.warn("Invalid yearly date for year {}: {}/{}", year, pattern.getYearlyMonth(), pattern.getYearlyDay());
            }
        }

        return occurrences;
    }
}
