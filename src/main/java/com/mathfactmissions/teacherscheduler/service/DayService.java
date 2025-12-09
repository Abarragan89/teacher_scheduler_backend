package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.model.*;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final UserService userService;
    private final ScheduleService scheduleService;

    public DayResponse findSingleDay(UUID dayId){
        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new RuntimeException("No Day found"));
        return DayResponse.fromEntity(day);
    }

    @Transactional
    public DayResponse createOrFindDay(UUID userId, LocalDate dayDate) {
        return dayRepository.findByUser_IdAndDayDate(userId, dayDate)
                .map(DayResponse::fromEntity)
                .orElseGet(() -> createNewDay(userId, dayDate));
    }

    @Transactional
    public DayResponse createOrFindDayWithSchedule(UUID userId, LocalDate dayDate, UUID scheduleId) {

        // Find or create the Day
        Day targetDay = dayRepository.findByUser_IdAndDayDate(userId, dayDate)
                .orElseGet(() -> {
                    User user = userService.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Day newDay = new Day();
                    newDay.setUser(user);
                    newDay.setDayDate(dayDate);
                    return dayRepository.save(newDay);
                });

        // Delete old schedule if it exists
        if (targetDay.getSchedule() != null) {
            targetDay.setSchedule(null);
            dayRepository.save(targetDay); // important to trigger orphan removal
        }

        // Find the source schedule
        Schedule sourceSchedule = scheduleService.findById(scheduleId);

        // Create the new schedule
        Schedule newSchedule = new Schedule();
        newSchedule.setDay(targetDay);

        // Copy tasks
        Set<Task> copiedTasks = sourceSchedule.getTasks().stream()
                .map(task -> {
                    Task newTask = new Task();
                    newTask.setTitle(task.getTitle());
                    newTask.setCompleted(false);
                    newTask.setPosition(task.getPosition());
                    newTask.setSchedule(newSchedule);

                    // Copy outline items
                    Set<TaskOutlineItem> copiedItems = task.getOutlineItems().stream()
                            .map(item -> {
                                TaskOutlineItem newItem = new TaskOutlineItem();
                                newItem.setText(item.getText());
                                newItem.setPosition(item.getPosition());
                                newItem.setCompleted(false);
                                newItem.setIndentLevel(item.getIndentLevel());
                                newItem.setTask(newTask);
                                return newItem;
                            })
                            .collect(Collectors.toSet());

                    newTask.setOutlineItems(copiedItems);
                    return newTask;
                })
                .collect(Collectors.toSet());

        newSchedule.setTasks(copiedTasks);

        // Attach schedule to day
        targetDay.setSchedule(newSchedule);

        // Save (assuming cascade persist)
        dayRepository.save(targetDay);

        // Return
        return DayResponse.fromEntity(targetDay);
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

    private DayResponse createNewDayWithPopulatedSchedule(UUID userId, LocalDate dayDate, UUID scheduleId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create Day
        Day newDay = new Day();
        newDay.setUser(user);
        newDay.setDayDate(dayDate);

        // Create Schedule
        Schedule schedule = scheduleService.findById(scheduleId);
        schedule.setDay(newDay);
        newDay.setSchedule(schedule);

        // Save Day with schedule embedded
        dayRepository.save(newDay);

        // Return DTO
        return DayResponse.fromEntity(newDay);
    }
}
