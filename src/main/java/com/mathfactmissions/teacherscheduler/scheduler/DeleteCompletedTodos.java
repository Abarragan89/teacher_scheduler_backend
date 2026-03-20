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
    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void purgeCompletedTodos() {
        Instant cutoff = Instant.now().minus(10, ChronoUnit.DAYS);
        todoRepository.deleteByCompletedTrueAndCompletedAtBefore(cutoff);
    }
}
