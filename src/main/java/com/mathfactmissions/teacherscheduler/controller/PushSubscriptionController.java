package com.mathfactmissions.teacherscheduler.controller;


import com.mathfactmissions.teacherscheduler.dto.PushSubscription.request.SubscriptionRequest;
import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class PushSubscriptionController {
    
    private final PushSubscriptionService pushSubscriptionService;
    
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(
        @AuthenticationPrincipal UserPrincipal userInfo,
        @RequestBody SubscriptionRequest request
    ) {
        PushSubscription subscription = pushSubscriptionService.saveSubscription(
            request.endpoint(),
            request.p256dhKey(),
            request.authKey(),
            userInfo.getId()
        );
        
        return ResponseEntity.ok(Map.of("success", true, "id", subscription.getId()));
    }
    
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody Map<String, String> request) {
        pushSubscriptionService.removeSubscription(request.get("endpoint"));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
