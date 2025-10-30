package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.model.Todo;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@Service
public class PushNotificationService {

    private final PushService pushService;
    private final PushSubscriptionService pushSubscriptionService;

    public PushNotificationService(
            @Value("${vapid.public.key:}") String vapidPublicKey,  // Added default empty string
            @Value("${vapid.private.key:}") String vapidPrivateKey,
            @Value("${vapid.subject:mailto:test@test.com}") String vapidSubject,
            PushSubscriptionService pushSubscriptionService
    ) {
        this.pushSubscriptionService = pushSubscriptionService;

        // Debug logging
        System.out.println("=== VAPID Configuration Debug ===");
        System.out.println("Public Key: '" + vapidPublicKey + "'");
        System.out.println("Private Key: '" + vapidPrivateKey + "'");
        System.out.println("Subject: '" + vapidSubject + "'");

        // Check if values are empty
        if (vapidPublicKey == null || vapidPublicKey.trim().isEmpty()) {
            throw new RuntimeException("VAPID public key is missing or empty!");
        }
        if (vapidPrivateKey == null || vapidPrivateKey.trim().isEmpty()) {
            throw new RuntimeException("VAPID private key is missing or empty!");
        }
        if (vapidSubject == null || vapidSubject.trim().isEmpty()) {
            throw new RuntimeException("VAPID subject is missing or empty!");
        }

        try {
            this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            System.out.println("✅ PushService created successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to create PushService: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("✅ Notification sent for todo: " + todoId);

            } catch (Exception e) {
                System.err.println("❌ Failed to send notification: " + e.getMessage());

                // Remove invalid subscriptions (expired/uninstalled app)
                if (e.getMessage().contains("410") || e.getMessage().contains("invalid")) {
                    System.out.println("🗑️ Removing invalid subscription: " + subscription.getEndpoint());
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