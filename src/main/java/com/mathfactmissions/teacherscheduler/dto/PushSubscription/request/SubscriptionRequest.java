package com.mathfactmissions.teacherscheduler.dto.PushSubscription.request;

public record SubscriptionRequest(
         String endpoint,
         String p256dhKey,
         String authKey
) {
}


