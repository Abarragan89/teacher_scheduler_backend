package com.mathfactmissions.teacherscheduler.controller;


import com.mathfactmissions.teacherscheduler.dto.PushSubscription.request.SubscriptionRequest;
import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.PushSubscriptionService;
import com.mathfactmissions.teacherscheduler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class PushSubscriptionController {


    private final PushSubscriptionService pushSubscriptionService;


    public PushSubscriptionController(
            PushSubscriptionService pushSubscriptionService
    ) {
        this.pushSubscriptionService = pushSubscriptionService;
    }


    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionRequest request, HttpServletRequest httpRequest) {
        try {
            // Get the currently authenticated user ID
            UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            UUID userId = userInfo.getId();

            PushSubscription subscription = pushSubscriptionService.saveSubscription(
                    request.endpoint(),
                    request.p256dhKey(),
                    request.authKey(),
                    userId
            );

            return ResponseEntity.ok(Map.of("success", true, "id", subscription.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> request) {
        try {
            pushSubscriptionService.removeSubscription(request.get("endpoint"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<PushSubscription>> getAllSubscriptions() {
        return ResponseEntity.ok(pushSubscriptionService.getAllSubscriptions());
    }
}
