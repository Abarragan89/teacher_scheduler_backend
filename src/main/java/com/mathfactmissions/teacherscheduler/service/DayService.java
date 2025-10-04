package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.dto.schedule.response.ScheduleResponse;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DayService {

    private final DayRepository dayRepository;
    private final UserService userService;
    private final ScheduleService scheduleService;

    @Autowired
    public DayService(
            DayRepository dayRepository,
            UserService userService,
            ScheduleService scheduleService
    ) {
        this.dayRepository = dayRepository;
        this.userService = userService;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public DayResponse createOrFindDay(UUID userId, LocalDate dayDate) {
        return dayRepository.findDayWithAllData(userId, dayDate)
                .map(this::mapToDayResponse)
                .orElseGet(() -> createNewDay(userId, dayDate));
    }

    private DayResponse createNewDay(UUID userId, LocalDate dayDate) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Day newDay = new Day();
        newDay.setUser(user);
        newDay.setDayDate(dayDate);

        Schedule schedule = new Schedule();
        schedule.setDay(newDay);
        newDay.getSchedules().add(schedule);
        dayRepository.save(newDay);

        return mapToDayResponse(newDay);
    }

    private DayResponse mapToDayResponse(Day day) {
        List<ScheduleResponse> scheduleResponses = day.getSchedules().stream()
                .map(schedule -> {
                    List<TaskResponse> taskResponses = schedule.getTasks().stream()
                            .map(task -> TaskResponse.builder()
                                    .id(task.getId())
                                    .title(task.getTitle())
                                    .position(task.getPosition())
                                    .completed(task.getCompleted())
                                    .build())
                            .toList();

                    return new ScheduleResponse(schedule.getId(), taskResponses);
                })
                .toList();

        return new DayResponse(day.getId(), day.getDayDate(), scheduleResponses);
    }


//    public List<DayResponse> findAllDays() {
//        List<Day> days = dayRepository.findAll();
//        return days.stream()
//                .map(day -> new DayResponse(day.getId(), day.getDayDate(), day.getSchedules()))
//                .toList();
//    }
}
