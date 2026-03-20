package com.mathfactmissions.teacherscheduler.scheduler;

import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DeleteCompletedTodos {
    
    private final TodoRepository todoRepository;
    
    @Modifying
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void purgeCompletedTodos() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        todoRepository.deleteByCompletedTrueAndCompletedAtBefore(cutoff);
    }
}
