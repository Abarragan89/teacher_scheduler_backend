package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.User;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PushNotificationService {
    
    private final PushService pushService;
    private final PushSubscriptionService pushSubscriptionService;
    
    public PushNotificationService(
        @Value("${vapid.public.key:}") String vapidPublicKey,
        @Value("${vapid.private.key:}") String vapidPrivateKey,
        @Value("${vapid.subject}") String vapidSubject,
        PushSubscriptionService pushSubscriptionService
    ) {
        this.pushSubscriptionService = pushSubscriptionService;
        
        try {
            this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            System.out.println("✅ PushService created successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to create PushService: " + e.getMessage());
            throw new RuntimeException("Failed to initialize PushService", e);
        }
    }
    
    public void sendTodoDueNotification(Todo todo) {
        List<PushSubscription> subscriptions = pushSubscriptionService.getSubscriptionsByUserId(todo.getUserId());
        
        String title = "Todo Due Soon!";
        
        String body = String.format("'%s'",
            todo.getText()
        );
        
        String dateString = todo.getDueDate()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        String targetUrl = String.format("/dashboard/daily/%s?view=todos", dateString);
        
        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "todoId": "%s",
                    "url": "%s"
                },
                "actions": [
                    {
                        "action": "mark-complete",
                        "title": "Mark Complete"
                    },
                    {
                        "action": "snooze",
                        "title": "Snooze 1hr"
                    }
                ]
            }
            """, escapeJson(title), escapeJson(body), todo.getId(), targetUrl);
        
        sendNotificationToSubscriptions(subscriptions, payload, todo.getId().toString());
    }
    
    public void sendTodoOverdueNotification(Todo todo) {
        List<PushSubscription> subscriptions = pushSubscriptionService.getSubscriptionsByUserId(todo.getUserId());
        
        String title = "Todo Overdue!";
        String body = String.format("'%s' was due and is now overdue", todo.getText());
        
        // Extract date from todo's due date and format it for the URL
        String dateString = todo.getDueDate()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        String targetUrl = String.format("/dashboard/daily/%s?view=todos", dateString);
        
        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "todoId": "%s",
                    "url": "%s",
                    "type": "todo_overdue"
                },
                "actions": [
                    {
                        "action": "mark-complete",
                        "title": "Mark Complete"
                    },
                    {
                        "action": "snooze",
                        "title": "Snooze 1hr"
                    }
                ]
            }
            """, escapeJson(title), escapeJson(body), todo.getId(), targetUrl);
        
        sendNotificationToSubscriptions(subscriptions, payload, todo.getId().toString());
    }
    
    private void sendNotificationToSubscriptions(List<PushSubscription> subscriptions, String payload, String todoId) {
        for (PushSubscription subscription : subscriptions) {
            try {
                Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dhKey(),
                    subscription.getAuthKey(),
                    payload.getBytes()
                );
                
                pushService.send(notification);
            } catch (Exception e) {
                // Remove invalid subscriptions (expired/uninstalled app)
                if (e.getMessage().contains("410") || e.getMessage().contains("invalid")) {
                    pushSubscriptionService.removeSubscription(subscription.getEndpoint());
                }
            }
        }
    }
    
    // Helper method to escape JSON strings
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    public void sendDailyReminderNotification(User user, List<TodoResponse> todos) {
        List<PushSubscription> subscriptions = pushSubscriptionService.getSubscriptionsByUserId(user.getId());
        
        String title = "Good Morning! Your TODOS 📋";
        
        // Build body summarizing todos
        String body;
        if (todos.size() == 1) {
            body = todos.get(0).text();
        } else {
            body = String.format("%d todos scheduled for today.",
                todos.size(),
                todos.get(0).text()
            );
        }
        
        // Link to today's daily view
        String dateString = LocalDate.now(user.getTimeZone())
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
        String targetUrl = String.format("/dashboard/todo-reminder-range?view=today", dateString);
        
        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "url": "%s"
                }
            }
            """, escapeJson(title), escapeJson(body), targetUrl);
        
        sendNotificationToSubscriptions(subscriptions, payload, user.getId().toString());
    }
    
    public void sendWeeklyUpcomingNotification(User user, List<TodoResponse> todos) {
        List<PushSubscription> subscriptions = pushSubscriptionService.getSubscriptionsByUserId(user.getId());
        
        String title = "Your week ahead 📅";
        
        String body;
        if (todos.size() == 1) {
            body = String.format("You have 1 todo coming up: %s", todos.get(0).text());
        } else {
            body = String.format("You have %d todos coming up over the next 10 days", todos.size());
        }
        
        // Link to today's date as entry point
        String dateString = LocalDate.now(user.getTimeZone())
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
        String targetUrl = String.format("/dashboard/todo-reminder-range?view=week", dateString);
        
        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "url": "%s"
                }
            }
            """, escapeJson(title), escapeJson(body), targetUrl);
        
        sendNotificationToSubscriptions(subscriptions, payload, user.getId().toString());
    }
}