package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserId(UUID userId)
            ;
    Optional<PushSubscription> findByEndpoint(String endpoint);

    @Modifying
    void deleteByEndpoint(String endpoint);

    List<PushSubscription> findAll(); // Get all subscriptions for broadcast
}
