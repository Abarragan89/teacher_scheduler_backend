package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.day.projections.DaySummary;
import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import com.mathfactmissions.teacherscheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DayService {

    private final DayRepository dayRepository;
    private final UserService userService;

    @Autowired
    public DayService(DayRepository dayRepository, UserService userService) {
        this.dayRepository = dayRepository;
        this.userService = userService;
    }


    public DayResponse createOrFindDay(UUID userId, LocalDate dayDate) {

        Optional<DaySummary> existing = dayRepository.findByUserIdAndDayDate(userId, dayDate);

        return existing
                .map(day -> new DayResponse(day.getId(), day.getDayDate()))
                .orElseGet(() -> {
                    Day newDay = new Day();
                    User user = userService.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    newDay.setUser(user);
                    newDay.setDayDate(dayDate);
                    dayRepository.save(newDay);
                    return new DayResponse(newDay.getId(), newDay.getDayDate());
                });
    }


    public List<DayResponse> findAllDays() {
        List<Day> days = dayRepository.findAll();
        return days.stream()
                .map(day -> new DayResponse(day.getId(), day.getDayDate()))
                .toList();
    }
}
