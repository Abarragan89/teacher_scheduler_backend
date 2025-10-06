package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.Day;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DayRepository extends JpaRepository<Day, UUID> {

    @EntityGraph(attributePaths = {"schedule", "schedule.tasks", "schedule.tasks.outlineItems"})
    Optional<Day> findByUser_IdAndDayDate(UUID userId, LocalDate dayDate);


}
