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
     * Check for todos due in the next 15 minutes
     * Runs every 5 minutes for good balance of precision vs efficiency
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes (300,000 milliseconds)
    public void checkDueTodos() {
        Instant now = Instant.now();

        System.out.println("now time " + now);

        Instant fifteenMinutesFromNow = now.plus(10, ChronoUnit.MINUTES);
        System.out.println("fifteen minutes" + fifteenMinutesFromNow);

        List<Todo> todosDueSoon = todoRepository.findTodosDueBetween(now, fifteenMinutesFromNow);

        System.out.println("📅 Checking for todos due soon... Found: " + todosDueSoon.size());

        for (Todo todo : todosDueSoon) {
            try {
                System.out.println("🔔 Sending notification for todo: " + todo.getText() + " (due at " + todo.getDueDate() + ")");

                // Send push notification
                pushNotificationService.sendTodoDueNotification(todo);

                // Mark as notified to prevent duplicate notifications
                todo.setNotificationSent(true);
                todo.setNotificationSentAt(Instant.now());
                todoRepository.save(todo);

                System.out.println("✅ Notification sent and todo marked as notified");

            } catch (Exception e) {
                System.err.println("❌ Failed to send notification for todo: " + todo.getId() + " - " + e.getMessage());
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

        System.out.println("⚠️ Checking for overdue todos... Found: " + overdueTodos.size());

        for (Todo todo : overdueTodos) {
            try {
                System.out.println("🚨 Sending overdue notification for todo: " + todo.getText());

                // Send overdue notification
                pushNotificationService.sendTodoOverdueNotification(todo);

                // Mark as overdue notification sent
                todo.setOverdueNotificationSent(true);
                todoRepository.save(todo);

                System.out.println("✅ Overdue notification sent");

            } catch (Exception e) {
                System.err.println("❌ Failed to send overdue notification for todo: " + todo.getId() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Optional: Log scheduler health every hour
     */
//    @Scheduled(cron = "0 30 * * * *") // Every hour at minute 30
//    public void logSchedulerHealth() {
//        Instant now = Instant.now();
//        Instant oneHourFromNow = now.plus(1, ChronoUnit.HOURS);
//
//        long upcomingCount = todoRepository.countTodosDueInNextHour(now, oneHourFromNow);
//        System.out.println("📊 Scheduler Health Check: " + upcomingCount + " todos due in next hour");
//    }
}