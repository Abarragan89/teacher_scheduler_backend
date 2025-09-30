package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.dto.day.projections.DaySummary;
import com.mathfactmissions.teacherscheduler.model.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DayRepository extends JpaRepository<Day, UUID> {
    Optional<DaySummary> findByUserIdAndDayDate(UUID userId, LocalDate dayDate);
}
