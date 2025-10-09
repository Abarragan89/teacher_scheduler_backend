package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import com.mathfactmissions.teacherscheduler.repository.PushSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public PushSubscriptionService(
            PushSubscriptionRepository pushSubscriptionRepository
    ) {
      this.pushSubscriptionRepository = pushSubscriptionRepository;
    }


    public PushSubscription saveSubscription(String endpoint, String p256dhKey, String authKey, UUID userId) {
        // Check if subscription already exists
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(endpoint);
        if (existing.isPresent()) {
            return existing.get(); // Already subscribed
        }

        PushSubscription subscription = new PushSubscription(endpoint, p256dhKey, authKey, userId);
        return pushSubscriptionRepository.save(subscription);
    }

    public void removeSubscription(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

    public List<PushSubscription> getAllSubscriptions() {
        return pushSubscriptionRepository.findAll();
    }

    public List<PushSubscription> getSubscriptionsByUserId(UUID userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }
}
