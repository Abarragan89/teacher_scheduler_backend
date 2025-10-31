package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.model.Todo;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
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
        String body = String.format("'%s' is due at %s",
                todo.getText(),
                todo.getDueDate().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a"))
        );

        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "todoId": "%s",
                    "url": "/dashboard"
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
            """, escapeJson(title), escapeJson(body), todo.getId());

        sendNotificationToSubscriptions(subscriptions, payload, todo.getId().toString());
    }

    public void sendTodoOverdueNotification(Todo todo) {
        List<PushSubscription> subscriptions = pushSubscriptionService.getSubscriptionsByUserId(todo.getUserId());

        String title = "Todo Overdue!";
        String body = String.format("'%s' was due and is now overdue", todo.getText());

        String payload = String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon-192x192.png",
                "badge": "/badge-72x72.png",
                "data": {
                    "todoId": "%s",
                    "url": "/dashboard"
                }
            }
            """, escapeJson(title), escapeJson(body), todo.getId());

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
}