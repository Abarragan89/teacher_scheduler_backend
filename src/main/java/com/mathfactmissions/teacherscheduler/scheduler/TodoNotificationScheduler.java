package com.mathfactmissions.teacherscheduler.scheduler;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import com.mathfactmissions.teacherscheduler.repository.UserRepository;
import com.mathfactmissions.teacherscheduler.service.PushNotificationService;
import com.mathfactmissions.teacherscheduler.service.RecurrencePatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TodoNotificationScheduler {
    
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;
    private final RecurrencePatternService recurrencePatternService;
    
    /**
     * Check for todos due in the next 10 minutes
     * Runs every 5 minutes for good balance of precision vs efficiency
     */
    @Scheduled(fixedRate = 30000) // Every 5 minutes (300,000 milliseconds)
    public void checkDueTodos() {
        Instant now = Instant.now();
        
        Instant tenMinutesFromNow = now.plus(10, ChronoUnit.MINUTES);
        
        List<Todo> todosDueSoon = todoRepository.findTodosDueBetween(now, tenMinutesFromNow);
        
        for (Todo todo : todosDueSoon) {
            try {
                // Send push notification
                pushNotificationService.sendTodoDueNotification(todo);
                
                // Mark as notified to prevent duplicate notifications
                todo.setNotificationSent(true);
                todo.setNotificationSentAt(Instant.now());
                todoRepository.save(todo);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification for todo: " + e.getMessage());
            }
        }
    }
    
    
    /**
     * Check for overdue todos that haven't been notified yet
     * Runs every hour to catch overdue items
     */
    @Scheduled(cron = "0 0,30 * * * *") // Every half-hour at minute 0
    public void checkOverdueTodos() {
        Instant now = Instant.now();
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(now);
        
        for (Todo todo : overdueTodos) {
            try {
                
                // Send overdue notification
                pushNotificationService.sendTodoOverdueNotification(todo);
                
                // Mark as overdue notification sent
                todo.setOverdueNotificationSent(true);
                todoRepository.save(todo);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to send overdue notification for todo: " + " - " + e.getMessage());
            }
        }
    }
    
    @Scheduled(cron = "0 0,15,30,45 * * * *")
    public void sendDailyMorningReminders() {
        LocalTime targetTime = LocalTime.of(6, 30);
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                ZoneId userZone = user.getTimeZone() != null
                    ? user.getTimeZone()
                    : ZoneId.of("UTC");
                
                LocalTime userLocalTime = LocalTime.now(userZone);
                LocalDate userLocalDate = LocalDate.now(userZone);
                
                // Check if it's within the 15 minute window of 6:30 AM
                if (userLocalTime.isAfter(targetTime.minusMinutes(1)) &&
                    userLocalTime.isBefore(targetTime.plusMinutes(14))) {
                    
                    List<TodoResponse> todaysTodos = getTodosForUserAndDate(
                        UUID.fromString(user.getId().toString()), userLocalDate, userZone
                    );
                    
                    if (!todaysTodos.isEmpty()) {
                        pushNotificationService.sendDailyReminderNotification(user, todaysTodos);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to send daily reminder for user: "
                    + user.getId() + " - " + e.getMessage());
            }
        }
    }
    
    @Scheduled(cron = "0 0,15,30,45 * * * 0")
    public void sendWeeklyUpcomingReminders() {
        LocalTime targetTime = LocalTime.of(20, 0);
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                ZoneId userZone = user.getTimeZone() != null
                    ? user.getTimeZone()
                    : ZoneId.of("UTC");
                
                LocalTime userLocalTime = LocalTime.now(userZone);
                LocalDate userLocalDate = LocalDate.now(userZone);
                
                // Make sure it's Sunday in the user's timezone
                if (userLocalDate.getDayOfWeek() != DayOfWeek.SUNDAY) continue;
                
                // Check if it's within the 15 minute window of 8:00 PM
                if (userLocalTime.isAfter(targetTime.minusMinutes(1)) &&
                    userLocalTime.isBefore(targetTime.plusMinutes(14))) {
                    
                    List<TodoResponse> upcomingTodos = getUpcomingTodosForUser(
                        user.getId(), userLocalDate, userLocalDate.plusDays(10), userZone
                    );
                    
                    if (!upcomingTodos.isEmpty()) {
                        pushNotificationService.sendWeeklyUpcomingNotification(user, upcomingTodos);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to send weekly reminder for user: "
                    + user.getId() + " - " + e.getMessage());
            }
        }
    }
    
    private List<TodoResponse> getTodosForUserAndDate(
        UUID userId, LocalDate date, ZoneId zone
    ) {
        Instant startOfDay = date.atStartOfDay(zone).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant();
        
        // Regular one-time todos
        List<TodoResponse> regularTodos = todoRepository
            .findByTodoList_User_IdAndDueDateBetweenAndCompletedFalse(
                userId, startOfDay, endOfDay
            )
            .stream()
            .map(TodoResponse::fromEntity)
            .toList();
        
        // Recurring todos — virtuals + overrides
        List<TodoResponse> recurringTodos = recurrencePatternService
            .getRecurringTodosInRange(userId, date, date)
            .stream()
            .filter(todo -> !todo.completed())
            .toList();
        
        List<TodoResponse> all = new ArrayList<>();
        all.addAll(regularTodos);
        all.addAll(recurringTodos);
        
        all.sort(Comparator.comparing(
            TodoResponse::dueDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
        
        return all;
    }
    
    private List<TodoResponse> getUpcomingTodosForUser(
        UUID userId, LocalDate from, LocalDate to, ZoneId zone
    ) {
        Instant startInstant = from.atStartOfDay(zone).toInstant();
        Instant endInstant = to.plusDays(1).atStartOfDay(zone).toInstant();
        
        List<TodoResponse> regularTodos = todoRepository
            .findByTodoList_User_IdAndDueDateBetweenAndCompletedFalse(
                userId, startInstant, endInstant
            )
            .stream()
            .map(TodoResponse::fromEntity)
            .toList();
        
        List<TodoResponse> recurringTodos = recurrencePatternService
            .getRecurringTodosInRange(userId, from, to)
            .stream()
            .filter(todo -> !todo.completed())
            .toList();
        
        List<TodoResponse> all = new ArrayList<>();
        all.addAll(regularTodos);
        all.addAll(recurringTodos);
        
        all.sort(Comparator.comparing(
            TodoResponse::dueDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
        
        return all;
    }
}