package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DayService {

    private final DayRepository dayRepository;
    private final UserService userService;

    @Autowired
    public DayService(
            DayRepository dayRepository,
            UserService userService
    ) {
        this.dayRepository = dayRepository;
        this.userService = userService;
    }

    @Transactional
    public DayResponse createOrFindDay(UUID userId, LocalDate dayDate) {
        return dayRepository.findByUser_IdAndDayDate(userId, dayDate)
                .map(DayResponse::fromEntity)
                .orElseGet(() -> createNewDay(userId, dayDate));
    }

    private DayResponse createNewDay(UUID userId, LocalDate dayDate) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create Day
        Day newDay = new Day();
        newDay.setUser(user);
        newDay.setDayDate(dayDate);

        // Create Schedule
        Schedule schedule = new Schedule();
        schedule.setDay(newDay);
        newDay.setSchedule(schedule);

        // Save Day with schedule embedded
        dayRepository.save(newDay);

        // Return DTO
        return DayResponse.fromEntity(newDay);
    }
}
