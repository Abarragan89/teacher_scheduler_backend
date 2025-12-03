package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.CreateRecurrencePatternRequest;
import com.mathfactmissions.teacherscheduler.dto.recurringTodos.response.RecurrencePatternDetails;
import com.mathfactmissions.teacherscheduler.dto.recurringTodos.response.RecurringTodoResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import com.mathfactmissions.teacherscheduler.repository.RecurrencePatternRepository;
import com.mathfactmissions.teacherscheduler.enums.RecurrenceType;
import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecurrencePatternService {

    private final TodoRepository todoRepository;
    private final TodoListRepository todoListRepository;
    private final RecurrencePatternRepository recurrencePatternRepository;

    public RecurrencePatternService
            (TodoRepository todoRepository,
             RecurrencePatternRepository recurrencePatternRepository,
             TodoListRepository todoListRepository
            ) {
        this.todoRepository = todoRepository;
        this.recurrencePatternRepository = recurrencePatternRepository;
        this.todoListRepository = todoListRepository;
    }


    /**
     * Create recurring todo and generate immediate first occurrence
     */
    @Transactional
    public RecurrencePattern createRecurrencePattern(CreateRecurrencePatternRequest request) {
        // Only create and return the pattern, do not create a Todo here
        RecurrencePattern pattern = buildRecurrencePattern(request);

        return recurrencePatternRepository.save(pattern);
    }

    /**
     * Build RecurrencePattern using Lombok builder based on request type
     */
    private RecurrencePattern buildRecurrencePattern(CreateRecurrencePatternRequest request) {
        RecurrencePattern.RecurrencePatternBuilder builder = RecurrencePattern.builder()
                .type(RecurrenceType.valueOf(request.recurrenceType().toUpperCase()))
                .timeOfDay(LocalTime.parse(request.time()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now());

        switch (request.recurrenceType().toUpperCase()) {
            case "WEEKLY":
                builder.daysOfWeek(String.join(",", request.selectedDays()));
                break;
            case "MONTHLY":
                if ("BY_DATE".equals(request.monthPatternType())) {
                    builder.monthPatternType(MonthPatternType.BY_DATE)
                            .daysOfMonth(String.join(",", request.selectedMonthDays()));
                } else {
                    builder.monthPatternType(MonthPatternType.BY_DAY)
                            .nthWeekdayOccurrence(request.nthWeekday().nth())
                            .nthWeekdayDay(request.nthWeekday().weekday());
                }
                break;
            case "YEARLY":
                LocalDate yearlyDate = request.yearlyDate();
                builder.yearlyMonth(yearlyDate.getMonthValue())
                        .yearlyDay(yearlyDate.getDayOfMonth());
                break;
            case "DAILY":
            default:
                // No extra fields needed
                break;
        }
        return builder.build();
    }

    /**
     * Generate occurrences immediately upon creation
     */
    public void generateImmediateOccurrences(Todo recurringTodo, TodoList todoList, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        List<LocalDate> occurrences = calculateOccurrences(
                recurringTodo.getRecurrencePattern(),
                today,
                endDate
        );

        for (LocalDate occurrence : occurrences) {
            createTodoInstance(recurringTodo, todoList, occurrence);
        }

        // Update last generated date
//        Todo updatedTodo = Todo.builder()
//                .lastGeneratedDate(endDate)
//
//                .todoList(todoList)
//                .updatedAt(Instant.now())
//                .build();

//        todoRepository.save(updatedTodo);

        log.info("Generated {} immediate occurrences for recurring todo: {}",
                occurrences.size(), recurringTodo.getId());
    }

    /**
     * Ensure we always have recurring todos generated for the next 30 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void ensureFutureTodosExist() {
        LocalDate today = LocalDate.now();
        LocalDate targetEndDate = today.plusDays(30);

        List<Todo> recurringTodos = todoRepository.findAllRecurringTodos();

        log.info("Running daily recurring todo top-up for {} recurring todos", recurringTodos.size());

        for (Todo recurringTodo : recurringTodos) {
            topUpFutureOccurrences(recurringTodo, targetEndDate);
        }

        log.info("Completed daily recurring todo top-up");
    }

    /**
     * Generate additional occurrences if we don't have enough future ones
     */
    private void topUpFutureOccurrences(Todo recurringTodo, LocalDate targetEndDate) {
        LocalDate lastGenerated = recurringTodo.getLastGeneratedDate();

        if (lastGenerated == null || lastGenerated.isBefore(targetEndDate)) {
            LocalDate generateFrom = lastGenerated != null ? lastGenerated.plusDays(1) : LocalDate.now();

            List<LocalDate> newOccurrences = calculateOccurrences(
                    recurringTodo.getRecurrencePattern(),
                    generateFrom,
                    targetEndDate
            );

//            for (LocalDate occurrence : newOccurrences) {
//                createTodoInstance(recurringTodo, todoList, occurrence);
//            }

            Todo updatedTodo = Todo.builder()
                    .lastGeneratedDate(targetEndDate)
                    .updatedAt(Instant.now())
                    .build();
            todoRepository.save(updatedTodo);

            log.debug("Generated {} additional occurrences for recurring todo: {}",
                    newOccurrences.size(), recurringTodo.getId());
        }
    }

    /**
     * Calculate all occurrences between start and end dates
     */
    private List<LocalDate> calculateOccurrences(RecurrencePattern pattern, LocalDate start, LocalDate end) {
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

    /**
     * Create individual todo instance using Lombok builder
     */
    private void createTodoInstance(Todo recurringTodo, TodoList todoList, LocalDate occurrence) {
        LocalDateTime occurrenceDateTime = occurrence.atTime(recurringTodo.getRecurrencePattern().getTimeOfDay());
        Instant dueDate = occurrenceDateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Check if instance already exists
//        boolean exists = todoRepository.existsByRecurringParentAndDueDate(recurringTodo, dueDate);
        System.out.println("In createTodoInstant (Text): " + recurringTodo.getText());

//        if (!exists) {
            Todo instance = Todo.builder()
                    .text(recurringTodo.getText())
                    .todoList(todoList)
                    .dueDate(dueDate)
                    .isRecurring(true) // Instance is not recurring itself
                    .recurrencePattern(recurringTodo.getRecurrencePattern()) // Track parent
                    .notificationSent(false)
                    .overdueNotificationSent(false)
                    .completed(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();


        todoRepository.save(instance);

            log.debug("Created todo instance for date: {} from recurring parent: {}",
                    occurrence, recurringTodo.getId());
//        }
    }

    /**
     * Convert Todo entity to response DTO
     */
    private RecurringTodoResponse toResponse(Todo todo) {
        return RecurringTodoResponse.builder()
                .id(todo.getId())
                .text(todo.getText())
                .todoList(todo.getTodoList())
                .recurrenceType(todo.getRecurrencePattern().getType().getValue())
                .timeOfDay(todo.getRecurrencePattern().getTimeOfDay())
                .lastGeneratedDate(todo.getLastGeneratedDate())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .patternDescription(generatePatternDescription(todo.getRecurrencePattern()))
                .patternDetails(toPatternDetails(todo.getRecurrencePattern()))
                .build();
    }

    /**
     * Convert RecurrencePattern to DTO
     */
    private RecurrencePatternDetails toPatternDetails(RecurrencePattern pattern) {
        return RecurrencePatternDetails.builder()
                .patternId(pattern.getId())
                .type(pattern.getType().getValue())
                .daysOfWeek(pattern.getDaysOfWeek())
                .monthPatternType(pattern.getMonthPatternType() != null ?
                        pattern.getMonthPatternType().getValue() : null)
                .daysOfMonth(pattern.getDaysOfMonth())
                .nthWeekdayOccurrence(pattern.getNthWeekdayOccurrence())
                .nthWeekdayDay(pattern.getNthWeekdayDay())
                .yearlyMonth(pattern.getYearlyMonth())
                .yearlyDay(pattern.getYearlyDay())
                .build();
    }

    /**
     * Generate human-readable pattern description
     */
    private String generatePatternDescription(RecurrencePattern pattern) {
        return switch (pattern.getType()) {
            case DAILY -> String.format("Daily at %s", pattern.getTimeOfDay());
            case WEEKLY -> String.format("Weekly on %s at %s",
                    formatWeekdays(pattern.getDaysOfWeek()), pattern.getTimeOfDay());
            case MONTHLY -> formatMonthlyDescription(pattern);
            case YEARLY -> String.format("Yearly on %s %d at %s",
                    Month.of(pattern.getYearlyMonth()).name(),
                    pattern.getYearlyDay(),
                    pattern.getTimeOfDay());
            default -> "Unknown pattern";
        };
    }

    private String formatWeekdays(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return "No days selected";
        }

        return Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(day -> switch (day) {
                    case 0 -> "Sunday";
                    case 1 -> "Monday";
                    case 2 -> "Tuesday";
                    case 3 -> "Wednesday";
                    case 4 -> "Thursday";
                    case 5 -> "Friday";
                    case 6 -> "Saturday";
                    default -> "Unknown";
                })
                .collect(Collectors.joining(", "));
    }

    private String formatMonthlyDescription(RecurrencePattern pattern) {
        if (pattern.getMonthPatternType() == MonthPatternType.BY_DATE) {
            if (pattern.getDaysOfMonth() == null || pattern.getDaysOfMonth().isEmpty()) {
                return "Monthly (no days specified)";
            }

            List<String> dayStrings = Arrays.stream(pattern.getDaysOfMonth().split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .map(day -> day == -1 ? "last day" : String.valueOf(day))
                    .collect(Collectors.toList());

            return String.format("Monthly on the %s at %s",
                    String.join(", ", dayStrings), pattern.getTimeOfDay());
        } else {
            String occurrence = switch (pattern.getNthWeekdayOccurrence()) {
                case 1 -> "1st";
                case 2 -> "2nd";
                case 3 -> "3rd";
                case 4 -> "4th";
                case -1 -> "last";
                default -> pattern.getNthWeekdayOccurrence() + "th";
            };
            String weekday = switch (pattern.getNthWeekdayDay()) {
                case 0 -> "Sunday";
                case 1 -> "Monday";
                case 2 -> "Tuesday";
                case 3 -> "Wednesday";
                case 4 -> "Thursday";
                case 5 -> "Friday";
                case 6 -> "Saturday";
                default -> "Unknown";
            };
            return String.format("Monthly on the %s %s at %s",
                    occurrence, weekday, pattern.getTimeOfDay());
        }
    }

    /**
     * Delete all future instances of a recurring todo
     */
//    @Transactional
//    public void deleteRecurringTodo(UUID recurringTodoId) {
//        Todo recurringTodo = todoRepository.findById(recurringTodoId)
//                .orElseThrow(() -> new IllegalArgumentException("Recurring todo not found"));
//
//        if (!recurringTodo.getIsRecurring()) {
//            throw new IllegalArgumentException("Todo is not a recurring todo");
//        }
//
//        // Delete all future instances
//        todoRepository.deleteByRecurringParentAndDueDateAfter(recurringTodo, Instant.now());
//
//        // Delete the recurring todo itself
//        todoRepository.delete(recurringTodo);
//
//        log.info("Deleted recurring todo: {} and all future instances", recurringTodoId);
//    }

    /**
     * Update recurring todo pattern and regenerate future instances
     */
//    @Transactional
//    public RecurringTodoResponse updateRecurringTodo(UUID recurringTodoId, CreateRecurringTodoRequest request) {
//        Todo recurringTodo = todoRepository.findById(recurringTodoId)
//                .orElseThrow(() -> new IllegalArgumentException("Recurring todo not found"));
//
//        // Delete all future instances
//        todoRepository.deleteByRecurringParentAndDueDateAfter(recurringTodo, Instant.now());
//
//        // Update the pattern
//        RecurrencePattern updatedPattern = buildRecurrencePattern(request);
//        RecurrencePattern savedPattern = recurrencePatternRepository.save(updatedPattern);
//
//        // Update the recurring todo using builder
//        Todo updatedTodo = Todo.builder()
//                .text(request.text())
//                .recurrencePattern(savedPattern)
//                .lastGeneratedDate(null) // Reset to regenerate
//                .updatedAt(Instant.now())
//                .build();
//
//        Todo savedTodo = todoRepository.save(updatedTodo);
//
//        // Generate new future instances
//        generateImmediateOccurrences(savedTodo, 30);
//
//        log.info("Updated recurring todo: {} with new pattern", recurringTodoId);
//
//        return toResponse(savedTodo);
//    }

    /**
     * Get all recurring todos for a user
     */
//    public List<RecurringTodoResponse> getRecurringTodosForUser(UUID userId) {
//        List<Todo> recurringTodos = todoRepository.findByUserAndIsRecurring(userId, true);
//        return recurringTodos.stream()
//                .map(this::toResponse)
//                .collect(Collectors.toList());
//    }
}