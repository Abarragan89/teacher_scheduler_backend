package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.TodoOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoOverrideRepository extends JpaRepository<TodoOverride, UUID> {
    
    List<TodoOverride> findByRecurrencePattern_IdAndOriginalDateBetween(
        UUID patternId, LocalDate from, LocalDate to
    );
    
    Optional<TodoOverride> findByRecurrencePattern_IdAndOriginalDate(
        UUID patternId, LocalDate date
    );
    
    void deleteByRecurrencePattern_IdAndOriginalDateGreaterThanEqual(
        UUID patternId, LocalDate fromDate
    );
}