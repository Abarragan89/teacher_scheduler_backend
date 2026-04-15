package com.mathfactmissions.teacherscheduler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.model.*;
import com.mathfactmissions.teacherscheduler.repository.DayRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    public DayResponse findSingleDay(UUID dayId, UUID userId) {
        Day day = dayRepository.findByIdAndUser_Id(dayId, userId)
            .orElseThrow(() -> new RuntimeException("Day not found for id: " + dayId));
        return DayResponse.fromEntity(day);
    }
    
    public DayResponse findSingleDayPublic(UUID userId, LocalDate dateString) {
        Day day = dayRepository.findByUser_IdAndDayDate(userId, dateString)
            .orElseThrow(() -> new RuntimeException("Day not found for user: " + userId + " on date: " + dateString));
        return DayResponse.fromEntity(day);
    }
    
    @Transactional
    public DayResponse createOrFindDay(UUID userId, LocalDate dayDate) {
        return dayRepository.findByUser_IdAndDayDate(userId, dayDate)
            .map(DayResponse::fromEntity)
            .orElseGet(() -> createNewDay(userId, dayDate));
    }
    
    @Transactional
    public void createOrFindDayWithSchedule(UUID userId, LocalDate dayDate, UUID scheduleId) {
        
        // Find or create the Day
        Day targetDay = dayRepository.findByUser_IdAndDayDate(userId, dayDate)
            .orElseGet(() -> {
                User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));
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
                newTask.setStartTime(task.getStartTime());
                newTask.setEndTime(task.getEndTime());
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
    
    
    public DayResponse updateNotes(UUID dayId, JsonNode notes) {
        Day day = dayRepository.findById(dayId)
            .orElseThrow(() -> new EntityNotFoundException("Day Not Found"));
        day.setNotes(notes);
        return DayResponse.fromEntity(dayRepository.save(day));
    }
}
