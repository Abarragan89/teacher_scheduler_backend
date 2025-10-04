package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.Day;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DayRepository extends JpaRepository<Day, UUID> {

    @Query("""
        SELECT d FROM Day d
        LEFT JOIN FETCH d.schedules s
        WHERE d.user.id = :userId AND d.dayDate = :date
    """)
    Optional<Day> findDayWithAllData(UUID userId, LocalDate date);

}
