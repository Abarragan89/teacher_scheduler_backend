package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.CreateRecurrencePatternRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.UpdateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.TodoOverride;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.RecurrencePatternRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoOverrideRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurrencePatternService {
    
    private final RecurrencePatternRepository recurrencePatternRepository;
    private final UserService userService;
    private final TodoListRepository todoListRepository;
    private final TodoRepository todoRepository;
    private final TodoOverrideRepository todoOverrideRepository;
    private final RecurrenceEngine recurrenceEngine;
    
    @Transactional
    public List<TodoResponse> createRecurrencePattern(CreateTodoRequest request, UUID userId) {
        
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        TodoList todoList = todoListRepository
            .findByIdAndUser_Id(request.todoListId(), userId)
            .orElseThrow(() -> new RuntimeException("Todo list not found or not owned"));
        
        RecurrencePattern pattern = RecurrencePattern.builder()
            .type(request.recurrencePattern().type())
            .timeOfDay(LocalTime.parse(request.recurrencePattern().timeOfDay()))
            .text(request.todoText())
            .timeZone(request.recurrencePattern().timeZone())
            .startDate(request.recurrencePattern().startDate())
            .endDate(request.recurrencePattern().endDate())
            .user(user)
            .todoList(todoList)
            .build();
        
        applyRecurrencePatternFields(pattern, request.recurrencePattern());
        
        recurrencePatternRepository.save(pattern);
        
        // Use the view range from the request so the frontend gets exactly
        // what it needs to render the month the user is currently viewing
        LocalDate from = request.viewStartDate();
        LocalDate to = request.viewEndDate();
        
        // If the pattern starts after the view start, use the pattern start date
        if (pattern.getStartDate().isAfter(from)) {
            from = pattern.getStartDate();
        }
        
        // Respect pattern end date
        if (pattern.getEndDate() != null && pattern.getEndDate().isBefore(to)) {
            to = pattern.getEndDate();
        }
        
        return computeOccurrencesInRange(pattern, from, to);
    }
    
    public List<TodoResponse> getRecurringTodosInRange(UUID userId, LocalDate from, LocalDate to) {
        List<RecurrencePattern> patterns = recurrencePatternRepository.findByUserId(userId);
        
        return patterns.stream()
            .flatMap(pattern -> {
                // Clamp range to pattern bounds
                LocalDate effectiveFrom = from.isBefore(pattern.getStartDate())
                    ? pattern.getStartDate() : from;
                
                LocalDate effectiveTo = pattern.getEndDate() != null && pattern.getEndDate().isBefore(to)
                    ? pattern.getEndDate() : to;
                
                if (effectiveFrom.isAfter(effectiveTo)) return java.util.stream.Stream.empty();
                
                return computeOccurrencesInRange(pattern, effectiveFrom, effectiveTo).stream();
            })
            .toList();
    }
    
    public List<TodoResponse> computeOccurrencesInRange(RecurrencePattern pattern, LocalDate from, LocalDate to) {
        
        Map<LocalDate, TodoOverride> overrides = todoOverrideRepository
            .findByRecurrencePattern_IdAndOriginalDateBetween(pattern.getId(), from, to)
            .stream()
            .collect(Collectors.toMap(TodoOverride::getOriginalDate, o -> o));
        
        return recurrenceEngine.calculateOccurrences(pattern, from, to)
            .stream()
            .map(date -> {
                TodoOverride override = overrides.get(date);
                if (override != null && override.isDeleted()) return null;
                if (override != null) return toOverrideResponse(override);
                return toVirtualResponse(pattern, date);
            })
            .filter(Objects::nonNull)
            .toList();
    }
    
    private TodoResponse toVirtualResponse(RecurrencePattern pattern, LocalDate date) {
        Instant dueDate = ZonedDateTime.of(date, pattern.getTimeOfDay(), pattern.getTimeZone())
            .toInstant();
        
        return TodoResponse.builder()
            .id("virtual_" + pattern.getId() + "_" + date)
            .patternId(pattern.getId())
            .text(pattern.getText())
            .dueDate(dueDate)
            .isRecurring(true)
            .todoListId(pattern.getTodoList().getId())
            .timeOfDay(pattern.getTimeOfDay())
            .completed(false)
            .isVirtual(true)
            .build();
    }
    
    private TodoResponse toOverrideResponse(TodoOverride override) {
        RecurrencePattern pattern = override.getRecurrencePattern();
        System.out.println("get custom due date" + override.getCustomDueDate());
//        Instant dueDate = override.getCustomDueDate() != null
//            ? override.getCustomDueDate()
//            : ZonedDateTime.of(override.getOriginalDate(), pattern.getTimeOfDay(), pattern.getTimeZone()).toInstant();
//

//        Instant dueDate = ZonedDateTime.of(
//            override.getOriginalDate(),
//            pattern.getTimeOfDay(),
//            pattern.getTimeZone()
//        ).toInstant();
        
        return TodoResponse.builder()
            .id(override.getId().toString())
            .patternId(pattern.getId())
            .text(override.getCustomTitle() != null ? override.getCustomTitle() : pattern.getText())
            .dueDate(override.getCustomDueDate())
            .todoListId(override.getTodoList().getId())
            .timeOfDay(pattern.getTimeOfDay())
            .isRecurring(true)
            .priority(override.getCustomPriority() != null ? override.getCustomPriority() : 1)
            .completed(override.isCompleted())
            .isVirtual(false)
            .build();
    }
    
    public List<TodoResponse> getNextOccurrenceForEachPattern(UUID userId) {
        List<RecurrencePattern> patterns = recurrencePatternRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        
        return patterns.stream()
            .map(pattern -> {
                // Don't show patterns that have already ended
                if (pattern.getEndDate() != null && pattern.getEndDate().isBefore(today)) {
                    return null;
                }
                
                LocalDate to = today.plusMonths(3); // look up to 3 months ahead for next occurrence
                
                // Respect pattern end date
                if (pattern.getEndDate() != null && pattern.getEndDate().isBefore(to)) {
                    to = pattern.getEndDate();
                }
                
                // Get the next occurrence date
                List<LocalDate> occurrences = recurrenceEngine.calculateOccurrences(pattern, today, to);
                if (occurrences.isEmpty()) return null;
                
                LocalDate nextDate = occurrences.get(0); // first upcoming occurrence
                
                // Check if there's an override for this date
                TodoOverride override = todoOverrideRepository
                    .findByRecurrencePattern_IdAndOriginalDate(pattern.getId(), nextDate)
                    .orElse(null);
                
                if (override != null && override.isDeleted()) {
                    // This occurrence was deleted, try the next one
                    return occurrences.stream()
                        .skip(1)
                        .map(date -> {
                            TodoOverride nextOverride = todoOverrideRepository
                                .findByRecurrencePattern_IdAndOriginalDate(pattern.getId(), date)
                                .orElse(null);
                            if (nextOverride != null && nextOverride.isDeleted()) return null;
                            if (nextOverride != null) return toOverrideResponse(nextOverride);
                            return toVirtualResponse(pattern, date);
                        })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                }
                
                if (override != null) return toOverrideResponse(override);
                return toVirtualResponse(pattern, nextDate);
            })
            .filter(Objects::nonNull)
            .toList();
    }
    
    public TodoResponse updateVirtualOccurrence(UpdateTodoRequest request) {
        UUID patternId = request.patternId();
        
        RecurrencePattern pattern = recurrencePatternRepository.findById(patternId)
            .orElseThrow(() -> new RuntimeException("Pattern not found"));
        
        // For virtual: parse date from id. For override: find by the real UUID
        TodoOverride override;
        
        if (request.todoId().startsWith("virtual_")) {
            // First time editing this occurrence — parse date from virtual id
            String[] parts = request.todoId().split("_", 3);
            LocalDate date = LocalDate.parse(parts[2]);
            
            TodoList todoList = pattern.getTodoList();
            
            if (!request.todoListId().equals(pattern.getTodoList().getId())) {
                todoList = todoListRepository.findById(request.todoListId())
                    .orElseThrow(() -> new RuntimeException("Todo list not found"));
            }
            
            override = todoOverrideRepository
                .findByRecurrencePattern_IdAndOriginalDate(patternId, date)
                .orElse(TodoOverride.builder()
                    .recurrencePattern(pattern)
                    .todoList(todoList)
                    .originalDate(date)
                    .build());
        } else {
            // Already an override row — find it by its real UUID
            override = todoOverrideRepository
                .findById(UUID.fromString(request.todoId()))
                .orElseThrow(() -> new RuntimeException("Override not found"));
            
            if (!request.todoListId().equals(override.getTodoList().getId())) {
                TodoList todoList = todoListRepository.findById(request.todoListId())
                    .orElseThrow(() -> new RuntimeException("Todo list not found"));
                override.setTodoList(todoList);
            }
        }
        
        override.setCustomTitle(request.todoText());
        override.setCompleted(request.completed() != null ? request.completed() : false);
        override.setCustomDueDate(request.dueDate());
        override.setCustomPriority(request.priority());
        
        
        todoOverrideRepository.save(override);
        return toOverrideResponse(override);
    }
    
    public List<TodoResponse> getTodosForDate(UUID userId, LocalDate date) {
        // Regular todos for this date
        List<TodoResponse> regularTodos = todoRepository
            .findByUserIdAndDate(userId, date)
            .stream()
            .map(TodoResponse::fromEntity)
            .toList();
        
        // Recurring todos (virtuals + overrides) for this date
        List<TodoResponse> recurringTodos = getRecurringTodosInRange(userId, date, date);
        
        List<TodoResponse> all = new ArrayList<>();
        all.addAll(regularTodos);
        all.addAll(recurringTodos);
        
        all.sort(Comparator.comparing(
            TodoResponse::dueDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
        
        return all;
    }
    
    
    @Transactional
    public TodoResponse updateAllFutureOccurrences(UpdateTodoRequest request) {
        UUID patternId = request.patternId();
        
        RecurrencePattern pattern = recurrencePatternRepository.findById(patternId)
            .orElseThrow(() -> new RuntimeException("Pattern not found"));
        
        LocalDate fromDate = request.dueDate()
            .atZone(pattern.getTimeZone())
            .toLocalDate();
        
        // Update text
        pattern.setText(request.todoText());
        pattern.setType(request.recurrencePattern().type());
        
        // Update time of day
        if (request.recurrencePattern().timeOfDay() != null) {
            pattern.setTimeOfDay(LocalTime.parse(request.recurrencePattern().timeOfDay()));
        }
        
        // Update timezone
        if (request.recurrencePattern().timeZone() != null) {
            pattern.setTimeZone(request.recurrencePattern().timeZone());
        }
        
        // Update start/end dates
        if (request.recurrencePattern().startDate() != null) {
            pattern.setStartDate(request.recurrencePattern().startDate());
        }
        
        if (request.recurrencePattern().endDate() != null) {
            pattern.setEndDate(request.recurrencePattern().endDate());
        }
        
        // Update recurrence type specific fields
        applyRecurrencePatternFields(pattern, request.recurrencePattern());
        
        recurrencePatternRepository.save(pattern);
        
        if (request.todoListId() != null && !request.todoListId().equals(pattern.getTodoList().getId())) {
            TodoList todoList = todoListRepository.findById(request.todoListId())
                .orElseThrow(() -> new RuntimeException("Todo list not found"));
            pattern.setTodoList(todoList);
        }
        
        todoOverrideRepository.deleteByRecurrencePattern_IdAndOriginalDateGreaterThanEqual(
            patternId, fromDate
        );
        
        return toVirtualResponse(pattern, fromDate);
    }
    
    // Shared helper — used in both create and update
    private void applyRecurrencePatternFields(RecurrencePattern pattern, CreateRecurrencePatternRequest request) {
        switch (pattern.getType()) {
            case WEEKLY:
                pattern.setDaysOfWeek(String.join(",", request.daysOfWeek()));
                break;
            case MONTHLY:
                if ("BY_DATE".equals(request.monthPatternType())) {
                    pattern.setMonthPatternType(MonthPatternType.BY_DATE);
                    pattern.setDaysOfMonth(String.join(",", request.daysOfMonth()));
                } else {
                    pattern.setMonthPatternType(MonthPatternType.BY_DAY);
                    pattern.setNthWeekdayOccurrence(request.nthWeekdayOccurrence().ordinal());
                    pattern.setNthWeekdayDay(request.nthWeekdayOccurrence().weekday());
                }
                break;
            case YEARLY:
                LocalDate yearlyDate = request.yearlyDate();
                pattern.setYearlyMonth(yearlyDate.getMonthValue());
                pattern.setYearlyDay(yearlyDate.getDayOfMonth());
                break;
            case DAILY:
            default:
                break;
        }
    }
}