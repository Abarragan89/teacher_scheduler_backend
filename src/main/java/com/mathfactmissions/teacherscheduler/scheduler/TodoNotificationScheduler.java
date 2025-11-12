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
    @Scheduled(fixedRate = 6000) // Every 5 minutes (300,000 milliseconds)
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
}