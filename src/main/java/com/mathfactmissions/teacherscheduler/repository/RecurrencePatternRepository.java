package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurrencePatternRepository extends JpaRepository<RecurrencePattern, Long> {

    List<RecurrencePattern> findByUserId(UUID userId);
}