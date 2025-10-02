package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

}
