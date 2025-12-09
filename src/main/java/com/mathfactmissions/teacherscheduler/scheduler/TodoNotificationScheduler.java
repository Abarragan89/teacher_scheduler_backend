package com.mathfactmissions.teacherscheduler.scheduler;

import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import com.mathfactmissions.teacherscheduler.service.PushNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TodoNotificationScheduler {

    private final TodoRepository todoRepository;
    private final PushNotificationService pushNotificationService;

    public TodoNotificationScheduler(
            TodoRepository todoRepository,
            PushNotificationService pushNotificationService
    ) {
        this.todoRepository = todoRepository;
        this.pushNotificationService = pushNotificationService;
    }

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



//    @Scheduled(fixedRate = 30000)
//    public void checkDueTodos() {
//        Instant now = Instant.now();
//        Instant tenMinutesFromNow = now.plus(10, ChronoUnit.MINUTES);
//
//        // 1. One-time todos (unchanged)
//        List<Todo> singleTodos =
//        todoRepository.findTodosDueBetween(now, tenMinutesFromNow);
//
//        singleTodos.forEach(this::notifyOnce);
//
//        // 2. Recurring todos (compute window)
//        List<Todo> recurringTodos =
//        todoRepository.findAllByIsRecurringTrue();
//
//        for (Todo todo : recurringTodos) {
//            RecurrencePattern pattern = todo.getRecurrencePattern();
//            ZoneId zone = ZoneId.of(pattern.getTimeZone());
//
//            // Convert UTC window to LOCAL window of the recurrence
//            LocalDate windowStart =
//            now.atZone(zone).toLocalDate();
//            LocalDate windowEnd =
//            tenMinutesFromNow.atZone(zone).toLocalDate();
//
//            List<LocalDate> dates =
//            calculateOccurrences(pattern, windowStart, windowEnd);
//
//            for (LocalDate date : dates) {
//                Instant occurrenceInstant =
//                ZonedDateTime.of(date, pattern.getTimeOfDay(), zone)
//                .toInstant();
//
//                if (!occurrenceInstant.isBefore(now)
//                && !occurrenceInstant.isAfter(tenMinutesFromNow)) {
//
//                    pushNotificationService.sendTodoDueNotification(todo, occurrenceInstant);
//                }
//            }
//        }
//    }


















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
}