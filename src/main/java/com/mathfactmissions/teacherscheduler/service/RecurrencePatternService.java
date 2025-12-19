package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.enums.MonthPatternType;
import com.mathfactmissions.teacherscheduler.enums.RecurrenceType;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.RecurrencePatternRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurrencePatternService {
    
    private final RecurrencePatternRepository recurrencePatternRepository;
    private final UserService userService;
    private final TodoListRepository todoListRepository;
    private final TodoService todoService;
    
    @Transactional
    public List<TodoResponse> createRecurrencePattern(CreateTodoRequest request, UUID userId) {
        
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        TodoList todoList = todoListRepository
            .findByIdAndUser_Id(request.todoListId(), userId)
            .orElseThrow(() -> new RuntimeException("Todo list not found or not owned"));
        
        RecurrencePattern pattern = RecurrencePattern.builder()
            .type(RecurrenceType.valueOf(request.recurrencePattern().type().toUpperCase()))
            .timeOfDay(LocalTime.parse(request.recurrencePattern().timeOfDay()))
            .text(request.todoText())
            .timeZone(request.recurrencePattern().timeZone())
            .startDate(request.recurrencePattern().startDate())
            .endDate(request.recurrencePattern().endDate())
            .user(user)
            .todoList(todoList)
            .build();
        
        // Set up the recurrence pattern rules based on patternType
        switch (pattern.getType()) {
            case WEEKLY:
                pattern.setDaysOfWeek(String.join(",", request.recurrencePattern().daysOfWeek()));
                break;
            case MONTHLY:
                if ("BY_DATE".equals(request.recurrencePattern().monthPatternType())) {
                    pattern.setMonthPatternType(MonthPatternType.BY_DATE);
                    pattern.setDaysOfMonth(
                        String.join(",", request.recurrencePattern().daysOfMonth())
                    );
                } else {
                    pattern.setMonthPatternType(MonthPatternType.BY_DAY);
                    pattern.setNthWeekdayOccurrence(request.recurrencePattern().nthWeekdayOccurrence().ordinal());
                    pattern.setNthWeekdayDay(request.recurrencePattern().nthWeekdayOccurrence().weekday());
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
        RecurrencePattern savedPattern = recurrencePatternRepository.save(pattern);
        
        LocalDate from = pattern.getStartDate();
        LocalDate to = from.plusMonths(2);
        return todoService.generateMissingTodosForPattern(pattern, from, to, request.todoText());
        
        
    }
}
