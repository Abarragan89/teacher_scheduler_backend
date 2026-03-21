package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {
    
    private final PushSubscriptionRepository pushSubscriptionRepository;
    
    public PushSubscription saveSubscription(String endpoint, String p256dhKey, String authKey, UUID userId) {
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(endpoint);
        if (existing.isPresent()) {
            System.out.println("ℹ️ Subscription already exists for user: " + userId);
            return existing.get();
        }
        
        PushSubscription subscription = new PushSubscription(endpoint, p256dhKey, authKey, userId);
        System.out.println("✅ New push subscription saved for user: " + userId);
        return pushSubscriptionRepository.save(subscription);
    }
    
    @Transactional
    public void removeSubscription(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
        System.out.println("🗑️ Removed push subscription for endpoint: " + endpoint);
    }
    
    public List<PushSubscription> getSubscriptionsByUserId(UUID userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }
}
